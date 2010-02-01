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

package org.springframework.ws.transport.support;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;

public class WebServiceMessageReceiverObjectSupportTest extends TestCase {

    private WebServiceMessageReceiverObjectSupport receiverSupport;

    private MockControl connectionControl;

    private WebServiceConnection connectionMock;

    private MockWebServiceMessageFactory messageFactory;

    private MockWebServiceMessage request;

    @Override
    protected void setUp() throws Exception {
        receiverSupport = new MyReceiverSupport();
        messageFactory = new MockWebServiceMessageFactory();
        receiverSupport.setMessageFactory(messageFactory);
        connectionControl = MockControl.createStrictControl(WebServiceConnection.class);
        connectionMock = (WebServiceConnection) connectionControl.getMock();
        request = new MockWebServiceMessage();
    }

    public void testHandleConnectionResponse() throws Exception {
        connectionControl.expectAndReturn(connectionMock.getUri(), new URI("http://example.com"));
        connectionControl.expectAndReturn(connectionMock.receive(messageFactory), request);
        connectionMock.send(new MockWebServiceMessage());
        connectionControl.setMatcher(MockControl.ALWAYS_MATCHER);
        connectionMock.close();

        connectionControl.replay();

        WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                assertNotNull("No message context", messageContext);
                messageContext.getResponse();
            }
        };

        receiverSupport.handleConnection(connectionMock, receiver);

        connectionControl.verify();
    }

    public void testHandleConnectionNoResponse() throws Exception {
        connectionControl.expectAndReturn(connectionMock.getUri(), new URI("http://example.com"));
        connectionControl.expectAndReturn(connectionMock.receive(messageFactory), request);
        connectionMock.close();

        connectionControl.replay();

        WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                assertNotNull("No message context", messageContext);
            }
        };

        receiverSupport.handleConnection(connectionMock, receiver);

        connectionControl.verify();
    }

    private static class MyReceiverSupport extends WebServiceMessageReceiverObjectSupport {

    }
}