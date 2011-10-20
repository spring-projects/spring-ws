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

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
public class WebServiceMessageReceiverObjectSupportTest {

    private WebServiceMessageReceiverObjectSupport receiverSupport;

    private WebServiceConnection connectionMock;

    private MockWebServiceMessageFactory messageFactory;

    private MockWebServiceMessage request;

    @Before
    public void setUp() throws Exception {
        receiverSupport = new MyReceiverSupport();
        messageFactory = new MockWebServiceMessageFactory();
        receiverSupport.setMessageFactory(messageFactory);
        connectionMock = createStrictMock(WebServiceConnection.class);
        request = new MockWebServiceMessage();
    }

    @Test
    public void testHandleConnectionResponse() throws Exception {
        expect(connectionMock.getUri()).andReturn(new URI("http://example.com"));
        expect(connectionMock.receive(messageFactory)).andReturn(request);
        connectionMock.send(isA(WebServiceMessage.class));
        connectionMock.close();

        replay(connectionMock);

        WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                Assert.assertNotNull("No message context", messageContext);
                messageContext.getResponse();
            }
        };

        receiverSupport.handleConnection(connectionMock, receiver);

        verify(connectionMock);
    }

    @Test
    public void testHandleConnectionNoResponse() throws Exception {
        expect(connectionMock.getUri()).andReturn(new URI("http://example.com"));
        expect(connectionMock.receive(messageFactory)).andReturn(request);
        connectionMock.close();

        replay(connectionMock);

        WebServiceMessageReceiver receiver = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                Assert.assertNotNull("No message context", messageContext);
            }
        };

        receiverSupport.handleConnection(connectionMock, receiver);

        verify(connectionMock);
    }

    private static class MyReceiverSupport extends WebServiceMessageReceiverObjectSupport {

    }
}