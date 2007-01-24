/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.samples.airline.client.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.activemq.ActiveMQConnection;
import org.codehaus.activemq.ActiveMQConnectionFactory;

/**
 * @author Arjen Poutsma
 */
public class GetFlights implements MessageListener {

    public static final String NAMESPACE_URI = "http://www.springframework.org/spring-ws/samples/airline/schemas";

    public static final String PREFIX = "airline";

    private static final String CORRELATION_ID = "correlationId";

    private static final String REQUEST_TOPIC = "org.springframework.ws.samples.airline.RequestTopic";

    private static final String RESPONSE_TOPIC = "org.springframework.ws.samples.airline.ResponseTopic";

    private TopicConnection connection;

    private MessageFactory messageFactory;

    private Topic responseTopic;

    private TopicSession session;

    private TransformerFactory transfomerFactory;

    public GetFlights(TopicConnectionFactory connectionFactory) throws SOAPException, JMSException {
        messageFactory = MessageFactory.newInstance();
        transfomerFactory = TransformerFactory.newInstance();
        connection = connectionFactory.createTopicConnection();
        session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        responseTopic = session.createTopic(RESPONSE_TOPIC);
        TopicSubscriber subscriber = session.createSubscriber(responseTopic);
        subscriber.setMessageListener(this);
        connection.start();
    }

    public void onMessage(Message message) {
        try {
            System.out.println("Received message");
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] buf = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(buf);
            ByteArrayInputStream is = new ByteArrayInputStream(buf);
            SOAPMessage saajMessage = messageFactory.createMessage(new MimeHeaders(), is);
            writeGetFlightsResponse(saajMessage);
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void close() {
        if (session != null) {
            try {
                session.close();
            }
            catch (JMSException ex) {
                ex.printStackTrace(System.err);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            }
            catch (JMSException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private SOAPMessage createGetFlightsRequest() throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name getFlightsRequestName = envelope.createName("GetFlightsRequest", PREFIX, NAMESPACE_URI);
        SOAPBodyElement getFlightsRequestElement = message.getSOAPBody().addBodyElement(getFlightsRequestName);
        Name fromName = envelope.createName("from", PREFIX, NAMESPACE_URI);
        SOAPElement fromElement = getFlightsRequestElement.addChildElement(fromName);
        fromElement.setValue("AMS");
        Name toName = envelope.createName("to", PREFIX, NAMESPACE_URI);
        SOAPElement toElement = getFlightsRequestElement.addChildElement(toName);
        toElement.setValue("VCE");
        Name departureDateName = envelope.createName("departureDate", PREFIX, NAMESPACE_URI);
        SOAPElement departureDateElement = getFlightsRequestElement.addChildElement(departureDateName);
        departureDateElement.setValue("2006-01-31");
        return message;
    }

    public void getFlights() throws SOAPException, IOException, TransformerException, JMSException {
        SOAPMessage request = createGetFlightsRequest();
        Topic requestTopic = session.createTopic(REQUEST_TOPIC);
        TopicPublisher publisher = session.createPublisher(requestTopic);
        BytesMessage message = session.createBytesMessage();
        message.setJMSCorrelationID(CORRELATION_ID);
        message.setJMSReplyTo(responseTopic);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        request.writeTo(os);
        os.flush();
        message.writeBytes(os.toByteArray());
        publisher.publish(message);
        System.out.println("Written GetFlights request to " + requestTopic);
    }

    private void writeGetFlightsResponse(SOAPMessage message) throws SOAPException, TransformerException {
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name getFlightsResponseName = envelope.createName("GetFlightsResponse", PREFIX, NAMESPACE_URI);
        SOAPBodyElement getFlightsResponseElement =
                (SOAPBodyElement) message.getSOAPBody().getChildElements(getFlightsResponseName).next();
        Name flightName = envelope.createName("flight", PREFIX, NAMESPACE_URI);
        Iterator iterator = getFlightsResponseElement.getChildElements(flightName);
        Transformer transformer = transfomerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        int count = 1;
        while (iterator.hasNext()) {
            System.out.println("Flight " + count);
            System.out.println("--------");
            SOAPElement flightElement = (SOAPElement) iterator.next();
            DOMSource source = new DOMSource(flightElement);
            transformer.transform(source, new StreamResult(System.out));
        }
    }

    public static void main(String[] args) throws Exception {
        String url = ActiveMQConnection.DEFAULT_URL;
        if (args.length > 0) {
            url = args[0];
        }
        TopicConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        GetFlights getFlights = null;
        try {
            getFlights = new GetFlights(connectionFactory);
            getFlights.getFlights();
            while (true) {
                // keep running until we receive a response message in onMessage
            }
        }
        finally {
            if (getFlights != null) {
                getFlights.close();
            }
        }
    }
}