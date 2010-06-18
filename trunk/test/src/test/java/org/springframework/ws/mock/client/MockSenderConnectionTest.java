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

package org.springframework.ws.mock.client;

import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** @author Arjen Poutsma */
public class MockSenderConnectionTest {

    private MockSenderConnection connection;

    @Before
    public void setUp() throws Exception {
        connection = new MockSenderConnection();
    }
    
    @Test
    public void uri() throws Exception {
        assertTrue("connection does not have any URI", connection.hasAnyUri());
        URI uri = new URI("http://example.com");
        connection = new MockSenderConnection(uri);
        assertEquals("Invalid uri", uri, connection.getUri());
    }

    @Test
    public void sendMatch() throws Exception {
        RequestMatcher requestMatcher1 = createMock("requestMatcher1", RequestMatcher.class);
        RequestMatcher requestMatcher2 = createMock("requestMatcher2", RequestMatcher.class);
        connection.addRequestMatcher(requestMatcher1);
        connection.addRequestMatcher(requestMatcher2);
        WebServiceMessage request = createMock("request", WebServiceMessage.class);

        requestMatcher1.match(request);
        requestMatcher2.match(request);

        replay(requestMatcher1, requestMatcher2, request);

        connection.send(request);


        verify(requestMatcher1, requestMatcher2, request);
    }

    @Test(expected = AssertionError.class)
    public void sendNonMatch() throws Exception {
        RequestMatcher requestMatcher1 = createMock("requestMatcher1", RequestMatcher.class);
        RequestMatcher requestMatcher2 = createMock("requestMatcher2", RequestMatcher.class);
        connection.addRequestMatcher(requestMatcher1);
        connection.addRequestMatcher(requestMatcher2);
        WebServiceMessage request = createMock("request", WebServiceMessage.class);

        requestMatcher1.match(request);
        expectLastCall().andThrow(new AssertionError());

        replay(requestMatcher1, requestMatcher2, request);

        connection.send(request);
    }

    
    @Test
    public void receive() throws Exception {
        ResponseCallback responseCallback = createMock(ResponseCallback.class);
        WebServiceMessageFactory messageFactory = createMock(WebServiceMessageFactory.class);
        WebServiceMessage response = createMock("response", WebServiceMessage.class);
        connection.setResponseCallback(responseCallback);

        expect(messageFactory.createWebServiceMessage()).andReturn(response);
        responseCallback.doWithResponse(response);

        replay(responseCallback, messageFactory, response);

        WebServiceMessage result = connection.receive(messageFactory);
        assertSame(response, result);

        verify(responseCallback, messageFactory, response);
    }
}
