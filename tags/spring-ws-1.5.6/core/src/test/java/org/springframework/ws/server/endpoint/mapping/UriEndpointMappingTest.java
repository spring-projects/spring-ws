/*
 * Copyright ${YEAR} the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

public class UriEndpointMappingTest extends TestCase {

    private UriEndpointMapping mapping;

    private MessageContext context;

    protected void setUp() throws Exception {
        mapping = new UriEndpointMapping();
        context = new DefaultMessageContext(new MockWebServiceMessageFactory());
    }

    public void testGetLookupKeyForMessage() throws Exception {
        MockControl control = MockControl.createControl(WebServiceConnection.class);
        WebServiceConnection connectionMock = (WebServiceConnection) control.getMock();
        TransportContextHolder.setTransportContext(new DefaultTransportContext(connectionMock));

        URI uri = new URI("jms://exampleQueue");
        control.expectAndReturn(connectionMock.getUri(), uri);
        control.replay();

        assertEquals("Invalid lookup key", uri.toString(), mapping.getLookupKeyForMessage(context));

        control.verify();
        TransportContextHolder.setTransportContext(null);
    }

    public void testValidateLookupKey() throws Exception {
        assertTrue("URI not valid", mapping.validateLookupKey("http://example.com/services"));
        assertFalse("URI not valid", mapping.validateLookupKey("some string"));
    }
}