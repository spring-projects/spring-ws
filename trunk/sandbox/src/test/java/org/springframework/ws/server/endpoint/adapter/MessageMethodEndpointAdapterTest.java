/*
 * Copyright 2007 the original author or authors.
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

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

public class MessageMethodEndpointAdapterTest extends TestCase {

    private MessageMethodEndpointAdapter adapter;

    private boolean supportedInvoked;

    private MessageContext messageContext;

    protected void setUp() throws Exception {
        adapter = new MessageMethodEndpointAdapter();
        messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
    }

    public void testSupported() throws NoSuchMethodException {
        Method noResponse = getClass().getMethod("supported", new Class[]{MessageContext.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
        assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
    }

    public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {
        Method unsupported = getClass()
                .getMethod("unsupportedMultipleParams", new Class[]{MessageContext.class, MessageContext.class});
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
    }

    public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedWrongParam", new Class[]{String.class});
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
    }

    public void testInvokeSupported() throws Exception {
        Method supported = getClass().getMethod("supported", new Class[]{MessageContext.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, supported);
        assertFalse("Method invoked", supportedInvoked);
        adapter.invoke(messageContext, methodEndpoint);
        assertTrue("Method not invoked", supportedInvoked);
    }

    public void supported(MessageContext context) {
        supportedInvoked = true;
    }

    public void unsupportedMultipleParams(MessageContext s1, MessageContext s2) {
    }

    public void unsupportedWrongParam(String request) {
    }
}