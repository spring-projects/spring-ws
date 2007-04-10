/*
*/
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
/*

package org.springframework.ws.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;

import junit.framework.TestCase;
import org.codehaus.activemq.message.ActiveMQBytesMessage;
import org.codehaus.activemq.message.ActiveMQTopic;
import org.easymock.MockControl;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

public class MessageEndpointMessageListenerTest extends TestCase {

    private static final String REQUEST = " <SOAP-ENV:Envelope\n" +
            "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "   <SOAP-ENV:Body>\n" +
            "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "           <symbol>DIS</symbol>\n" +
            "       </m:GetLastTradePrice>\n" + "   </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    private WebServiceMessageReceiverMessageListener messageListener;

    private BytesMessage request;

    private MockControl sessionControl;

    private Session sessionMock;

    protected void setUp() throws Exception {
        messageListener = new WebServiceMessageReceiverMessageListener();
        request = new ActiveMQBytesMessage();
        request.writeBytes(REQUEST.getBytes("UTF-8"));
        messageListener.setMessageFactory(new MockWebServiceMessageFactory());
        sessionControl = MockControl.createControl(Session.class);
        sessionMock = (Session) sessionControl.getMock();
    }

    public void testOnMessageInvalidMessage() throws Exception {
        MockControl mockControl = MockControl.createControl(StreamMessage.class);
        StreamMessage message = (StreamMessage) mockControl.getMock();
        try {
            messageListener.onMessage(message, sessionMock);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testOnMessageNoResponse() throws Exception {

        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
            }
        };
        messageListener.setMessageReceiver(endpoint);

        request.reset();
        messageListener.onMessage(request, sessionMock);
    }

    public void testOnMessageResponse() throws Exception {
        MockControl producerControl = MockControl.createControl(MessageProducer.class);
        MessageProducer producerMock = (MessageProducer) producerControl.getMock();
        BytesMessage response = new ActiveMQBytesMessage();
        String correlationId = "correlationId";
        Destination replyTo = new ActiveMQTopic();
        request.setJMSCorrelationID(correlationId);
        request.setJMSReplyTo(replyTo);
        request.reset();
        sessionControl.expectAndReturn(sessionMock.createBytesMessage(), response);
        sessionControl.expectAndReturn(sessionMock.createProducer(replyTo), producerMock);
        producerMock.marshalSendAndReceive(response);
        sessionControl.replay();
        producerControl.replay();

        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
                messageContext.getResponse();
            }
        };
        messageListener.setMessageReceiver(endpoint);

        messageListener.onMessage(request, sessionMock);

        sessionControl.verify();
        producerControl.verify();
        assertEquals("Invalid correlationId", correlationId, response.getJMSCorrelationID());
    }

}*/
