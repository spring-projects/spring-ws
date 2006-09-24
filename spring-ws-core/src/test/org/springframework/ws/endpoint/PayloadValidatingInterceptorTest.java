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
import org.easymock.MockControl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockWebServiceMessage;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.AbstractSoapMessageContext;
import org.springframework.ws.soap.context.SoapMessageContext;
import org.springframework.xml.transform.StringSource;

public class PayloadValidatingInterceptorTest extends TestCase {

    private PayloadValidatingInterceptor interceptor;

    private static final String VALID_MESSAGE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><product xmlns=\"http://www.springframework.org/spring-ws/test/validation\" effDate=\"2006-01-01\"><number>42</number><size>10</size></product>";

    private static final String INVALID_MESSAGE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><product xmlns=\"http://www.springframework.org/spring-ws/test/validation\" effDate=\"2006-01-01\"><size>20</size></product>";

    private MockWebServiceMessage request;

    private MockMessageContext messageContext;

    protected void setUp() throws Exception {
        interceptor = new PayloadValidatingInterceptor();
        interceptor.setSchema(new ClassPathResource("schema.xsd", PayloadValidatingInterceptorTest.class));
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        interceptor.afterPropertiesSet();
        request = new MockWebServiceMessage();
        messageContext = new MockMessageContext(request);
    }

    public void testHandleValidRequest() throws Exception {
        request.setPayload(VALID_MESSAGE);
        boolean result = interceptor.handleRequest(messageContext, null);
        assertTrue("Invalid response from interceptor", result);
        assertNull("Response set", messageContext.getResponse());
    }

    public void testHandleInvalidRequest() throws Exception {
        MockControl soapMessageControl = MockControl.createControl(SoapMessage.class);
        final SoapMessage requestMock = (SoapMessage) soapMessageControl.getMock();
        final SoapMessage responseMock = (SoapMessage) soapMessageControl.getMock();
        SoapMessageContext soapMessageContext = new AbstractSoapMessageContext() {
            public SoapMessage getSoapRequest() {
                return requestMock;
            }

            public SoapMessage createSoapResponse() {
                return responseMock;
            }

            public SoapMessage getSoapResponse() {
                return responseMock;
            }
        };
        MockControl soapBodyControl = MockControl.createControl(SoapBody.class);
        SoapBody soapBodyMock = (SoapBody) soapBodyControl.getMock();
        soapMessageControl.expectAndReturn(requestMock.getPayloadSource(), new StringSource(INVALID_MESSAGE));
        soapMessageControl.expectAndReturn(responseMock.getSoapBody(), soapBodyMock);
        MockControl soapFaultControl = MockControl.createControl(SoapFault.class);
        SoapFault soapFaultMock = (SoapFault) soapFaultControl.getMock();
        soapBodyControl.expectAndReturn(soapBodyMock.addSenderFault("Validation error"), soapFaultMock);

        soapMessageControl.replay();
        soapBodyControl.replay();
        soapFaultControl.replay();

        boolean result = interceptor.handleRequest(soapMessageContext, null);
        assertFalse("Invalid response from interceptor", result);
        soapMessageControl.verify();
        soapBodyControl.verify();
        soapFaultControl.verify();
    }

    public void testHandleValidResponse() throws Exception {
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.createResponse();
        response.setPayload(VALID_MESSAGE);
        boolean result = interceptor.handleResponse(messageContext, null);
        assertTrue("Invalid response from interceptor", result);
    }

    public void testHandleInvalidResponse() throws Exception {
        MockWebServiceMessage response = (MockWebServiceMessage) messageContext.createResponse();
        response.setPayload(INVALID_MESSAGE);
        boolean result = interceptor.handleResponse(messageContext, null);
        assertFalse("Invalid response from interceptor", result);
    }
}