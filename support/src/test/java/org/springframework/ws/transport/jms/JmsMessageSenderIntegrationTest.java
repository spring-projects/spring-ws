/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.transport.jms;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

public class JmsMessageSenderIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

    private JmsMessageSender messageSender;

    private JmsTemplate jmsTemplate;

    private MessageFactory messageFactory;

    private static final String SOAP_ACTION = "\"http://springframework.org/DoIt\"";

    @Override
    protected void onSetUp() throws Exception {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"classpath:org/springframework/ws/transport/jms/jms-sender-applicationContext.xml"};
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void setMessageSender(JmsMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void testSendAndReceiveQueueBytesMessage() throws Exception {
        WebServiceConnection connection = null;
        try {
            URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");
            connection = messageSender.createConnection(uri);
            SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
            soapRequest.setSoapAction(SOAP_ACTION);
            connection.send(soapRequest);

            BytesMessage request = (BytesMessage) jmsTemplate.receive();
            assertNotNull("No message received", request);
            assertTrue("No message content received", request.readByte() != -1);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            messageFactory.createMessage().writeTo(bos);
            final byte[] buf = bos.toByteArray();
            jmsTemplate.send(request.getJMSReplyTo(), new MessageCreator() {

                public Message createMessage(Session session) throws JMSException {
                    BytesMessage response = session.createBytesMessage();
                    response.setStringProperty(JmsTransportConstants.PROPERTY_SOAP_ACTION, SOAP_ACTION);
                    response.setStringProperty(JmsTransportConstants.PROPERTY_CONTENT_TYPE,
                            SoapVersion.SOAP_11.getContentType());
                    response.writeBytes(buf);
                    return response;
                }
            });
            SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
            assertNotNull("No response received", response);
            assertEquals("Invalid SOAPAction", SOAP_ACTION, response.getSoapAction());
            assertFalse("Message is fault", response.hasFault());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void testSendAndReceiveQueueTextMessage() throws Exception {
        WebServiceConnection connection = null;
        try {
            URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT&messageType=TEXT_MESSAGE");
            connection = messageSender.createConnection(uri);
            SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
            soapRequest.setSoapAction(SOAP_ACTION);
            connection.send(soapRequest);

            TextMessage request = (TextMessage) jmsTemplate.receive();
            assertNotNull("No message received", request);
            assertNotNull("No message content received", request.getText());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            messageFactory.createMessage().writeTo(bos);
            final String text = new String(bos.toByteArray(), "UTF-8");
            jmsTemplate.send(request.getJMSReplyTo(), new MessageCreator() {

                public Message createMessage(Session session) throws JMSException {
                    TextMessage response = session.createTextMessage();
                    response.setStringProperty(JmsTransportConstants.PROPERTY_SOAP_ACTION, SOAP_ACTION);
                    response.setStringProperty(JmsTransportConstants.PROPERTY_CONTENT_TYPE,
                            SoapVersion.SOAP_11.getContentType());
                    response.setText(text);
                    return response;
                }
            });
            SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
            assertNotNull("No response received", response);
            assertEquals("Invalid SOAPAction", SOAP_ACTION, response.getSoapAction());
            assertFalse("Message is fault", response.hasFault());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void testSendNoResponse() throws Exception {
        WebServiceConnection connection = null;
        try {
            URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");
            connection = messageSender.createConnection(uri);
            SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
            soapRequest.setSoapAction(SOAP_ACTION);
            connection.send(soapRequest);

            BytesMessage request = (BytesMessage) jmsTemplate.receive();
            assertNotNull("No message received", request);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            messageFactory.createMessage().writeTo(bos);
            SoapMessage response = (SoapMessage) connection.receive(new SaajSoapMessageFactory(messageFactory));
            assertNull("Response received", response);
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void testPostProcessor() throws Exception {
        MessagePostProcessor processor = new MessagePostProcessor() {
            public Message postProcessMessage(Message message) throws JMSException {
                message.setBooleanProperty("processed", true);
                return message;
            }
        };
        JmsSenderConnection connection = null;
        try {
            URI uri = new URI("jms:SenderRequestQueue?deliveryMode=NON_PERSISTENT");
            connection = (JmsSenderConnection) messageSender.createConnection(uri);
            connection.setPostProcessor(processor);
            SoapMessage soapRequest = new SaajSoapMessage(messageFactory.createMessage());
            connection.send(soapRequest);

            BytesMessage request = (BytesMessage) jmsTemplate.receive();
            assertNotNull("No message received", request);
            assertTrue("Message not processed", request.getBooleanProperty("processed"));
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }

    }
}