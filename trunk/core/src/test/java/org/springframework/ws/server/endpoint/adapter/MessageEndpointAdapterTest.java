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

package org.springframework.ws.server.endpoint.adapter;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

public class MessageEndpointAdapterTest extends TestCase {

    private MessageEndpointAdapter adapter;

    private MockControl endpointControl;

    private MessageEndpoint endpointMock;

    @Override
    protected void setUp() throws Exception {
        adapter = new MessageEndpointAdapter();
        endpointControl = MockControl.createControl(MessageEndpoint.class);
        endpointMock = (MessageEndpoint) endpointControl.getMock();
    }

    public void testSupports() throws Exception {
        assertTrue("MessageEndpointAdapter does not support MessageEndpoint", adapter.supports(endpointMock));
    }

    public void testInvoke() throws Exception {
        MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());

        endpointMock.invoke(context);
        endpointControl.replay();
        adapter.invoke(context, endpointMock);
        endpointControl.verify();
    }

}