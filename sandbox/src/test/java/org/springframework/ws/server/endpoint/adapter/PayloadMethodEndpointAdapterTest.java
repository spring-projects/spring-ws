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
import javax.xml.transform.Source;

import junit.framework.TestCase;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

public class PayloadMethodEndpointAdapterTest extends TestCase {

    private PayloadMethodEndpointAdapter adapter;

    private boolean noResponseInvoked;

    private boolean responseInvoked;

    private MessageContext messageContext;

    protected void setUp() throws Exception {
        adapter = new PayloadMethodEndpointAdapter();
        messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
    }

    public void testSupportedNoResponse() throws NoSuchMethodException {
        Method noResponse = getClass().getMethod("noResponse", new Class[]{Source.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
        assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
    }

    public void testSupportedResponse() throws NoSuchMethodException {
        Method response = getClass().getMethod("response", new Class[]{Source.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
        assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
    }

    public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedMultipleParams", new Class[]{Source.class, Source.class});
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
    }

    public void testUnsupportedMethodWrongReturnType() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedWrongReturnType", new Class[]{Source.class});
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
    }

    public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {
        Method unsupported = getClass().getMethod("unsupportedWrongParam", new Class[]{String.class});
        assertFalse("Method supported", adapter.supportsInternal(new MethodEndpoint(this, unsupported)));
    }

    public void testNoResponse() throws Exception {
        Method noResponse = getClass().getMethod("noResponse", new Class[]{Source.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, noResponse);
        assertFalse("Method invoked", noResponseInvoked);
        adapter.invoke(messageContext, methodEndpoint);
        assertTrue("Method not invoked", noResponseInvoked);
    }

    public void testResponse() throws Exception {
        WebServiceMessage request = new MockWebServiceMessage("<request/>");
        messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
        Method response = getClass().getMethod("response", new Class[]{Source.class});
        MethodEndpoint methodEndpoint = new MethodEndpoint(this, response);
        assertFalse("Method invoked", responseInvoked);
        adapter.invoke(messageContext, methodEndpoint);
        assertTrue("Method not invoked", responseInvoked);
    }

    public void noResponse(Source request) {
        noResponseInvoked = true;

    }

    public Source response(Source request) {
        responseInvoked = true;
        return request;
    }

    public void unsupportedMultipleParams(Source s1, Source s2) {
    }

    public Source unsupportedWrongParam(String request) {
        return null;
    }

    public String unsupportedWrongReturnType(Source request) {
        return null;
    }

}