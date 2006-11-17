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

package org.springframework.ws.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.MessageEndpoint;

public class JmsTransportMessageListenerTest extends TestCase {

    private static final String REQUEST = " <SOAP-ENV:Envelope\n" +
            "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "   <SOAP-ENV:Body>\n" +
            "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "           <symbol>DIS</symbol>\n" +
            "       </m:GetLastTradePrice>\n" + "   </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    private JmsTransportMessageListener messageListener;

    private MockControl messageControl;

    private TextMessage requestMock;

    protected void setUp() throws Exception {
        messageListener = new JmsTransportMessageListener();
        messageControl = MockControl.createControl(TextMessage.class);
        requestMock = (TextMessage) messageControl.getMock();
        messageListener.setMessageFactory(new MockWebServiceMessageFactory());
    }

    public void testOnMessageInvalidMessage() throws Exception {
        MockControl mockControl = MockControl.createControl(BytesMessage.class);
        BytesMessage bytesMessage = (BytesMessage) mockControl.getMock();
        try {
            messageListener.onMessage(bytesMessage);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testOnMessageNoResponse() throws Exception {
        messageControl.expectAndReturn(requestMock.getText(), REQUEST);
        messageControl.replay();

        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
            }
        };
        messageListener.setMessageEndpoint(endpoint);

        messageListener.onMessage(requestMock);

        messageControl.verify();
    }

    public void testOnMessageResponse() throws Exception {
        MockControl sessionControl = MockControl.createControl(Session.class);
        Session sessionMock = (Session) sessionControl.getMock();
        MockControl producerControl = MockControl.createControl(MessageProducer.class);
        MessageProducer producerMock = (MessageProducer) producerControl.getMock();
        TextMessage responseMock = (TextMessage) messageControl.getMock();
        messageControl.expectAndReturn(requestMock.getText(), REQUEST);
        String correlationId = "correlationId";
        Destination replyTo = new Destination() {
        };
        messageControl.expectAndReturn(requestMock.getJMSCorrelationID(), correlationId);
        sessionControl.expectAndReturn(sessionMock.createTextMessage(), responseMock);
        responseMock.setJMSCorrelationID(correlationId);
        messageControl.expectAndReturn(requestMock.getJMSReplyTo(), replyTo);
        sessionControl.expectAndReturn(sessionMock.createProducer(replyTo), producerMock);
        producerMock.send(responseMock);
        messageControl.replay();
        sessionControl.replay();
        producerControl.replay();

        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
                messageContext.getResponse();
            }
        };
        messageListener.setMessageEndpoint(endpoint);

        messageListener.onMessage(requestMock, sessionMock);

        messageControl.verify();
        sessionControl.verify();
        producerControl.verify();
    }
}