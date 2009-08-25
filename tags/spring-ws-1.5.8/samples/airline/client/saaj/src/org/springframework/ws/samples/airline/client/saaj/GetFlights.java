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

package org.springframework.ws.samples.airline.client.saaj;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Simple client that calls the <code>GetFlights</code> operation using SAAJ.
 *
 * @author Arjen Poutsma
 */
public class GetFlights {

    public static final String NAMESPACE_URI = "http://www.springframework.org/spring-ws/samples/airline/schemas/messages";

    public static final String PREFIX = "airline";

    private SOAPConnectionFactory connectionFactory;

    private MessageFactory messageFactory;

    private URL url;

    private TransformerFactory transfomerFactory;

    public GetFlights(String url) throws SOAPException, MalformedURLException {
        connectionFactory = SOAPConnectionFactory.newInstance();
        messageFactory = MessageFactory.newInstance();
        transfomerFactory = TransformerFactory.newInstance();
        this.url = new URL(url);
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

    public void getFlights() throws SOAPException, IOException, TransformerException {
        SOAPMessage request = createGetFlightsRequest();
        SOAPConnection connection = connectionFactory.createConnection();
        SOAPMessage response = connection.call(request, url);
        if (!response.getSOAPBody().hasFault()) {
            writeGetFlightsResponse(response);
        }
        else {
            SOAPFault fault = response.getSOAPBody().getFault();
            System.err.println("Received SOAP Fault");
            System.err.println("SOAP Fault Code:   " + fault.getFaultCode());
            System.err.println("SOAP Fault String: " + fault.getFaultString());
        }
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
        String url = "http://localhost:8080/airline/services";
        if (args.length > 0) {
            url = args[0];
        }
        GetFlights getFlights = new GetFlights(url);
        getFlights.getFlights();
    }
}
