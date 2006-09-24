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

package org.springframework.ws.endpoint;

import junit.framework.TestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.mock.soap.MockSoapMessage;
import org.springframework.ws.mock.soap.MockSoapMessageContext;

public class PayloadValidatingInterceptorTest extends TestCase {

    private PayloadValidatingInterceptor interceptor;

    private static final String VALID_MESSAGE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><product xmlns=\"http://www.springframework.org/spring-ws/test/validation\" effDate=\"2006-01-01\"><number>42</number><size>10</size></product>";

    private static final String INVALID_MESSAGE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><product xmlns=\"http://www.springframework.org/spring-ws/test/validation\" effDate=\"2006-01-01\"><size>20</size></product>";

    private MockSoapMessage request;

    private MockSoapMessageContext messageContext;

    protected void setUp() throws Exception {
        interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource("schema.xsd", PayloadValidatingInterceptorTest.class));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        interceptor.afterPropertiesSet();
        request = new MockSoapMessage();
        messageContext = new MockSoapMessageContext(request);
    }

    public void testHandleValidRequest() throws Exception {
        request.setPayload(VALID_MESSAGE);
        boolean result = interceptor.handleRequest(messageContext, null);
        assertTrue("Invalid response from interceptor", result);
        assertNull("Response set", messageContext.getResponse());
    }

    public void testHandleInvalidRequest() throws Exception {
        request.setPayload(INVALID_MESSAGE);
        boolean result = interceptor.handleRequest(messageContext, null);
        assertFalse("Invalid response from interceptor", result);
        assertNotNull("No Response set", messageContext.getSoapResponse());
        assertNotNull("No fault set in response", messageContext.getSoapResponse().getFault());
    }

    public void testHandleValidResponse() throws Exception {
        MockSoapMessage response = (MockSoapMessage) messageContext.createResponse();
        response.setPayload(VALID_MESSAGE);
        boolean result = interceptor.handleResponse(messageContext, null);
        assertTrue("Invalid response from interceptor", result);
    }

    public void testHandleInvalidResponse() throws Exception {
        MockSoapMessage response = (MockSoapMessage) messageContext.createResponse();
        response.setPayload(INVALID_MESSAGE);
        boolean result = interceptor.handleResponse(messageContext, null);
        assertFalse("Invalid response from interceptor", result);
    }
}