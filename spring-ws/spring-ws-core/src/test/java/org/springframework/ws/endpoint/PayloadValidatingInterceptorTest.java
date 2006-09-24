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
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
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
        MockControl messageControl = MockControl.createControl(SoapMessage.class);
        MockControl bodyControl = MockControl.createControl(SoapBody.class);
        SoapBody bodyMock = (SoapBody) bodyControl.getMock();
        MockControl faultControl = MockControl.createControl(SoapFault.class);
        SoapFault faultMock = (SoapFault) faultControl.getMock();
        MockControl faultDetailControl = MockControl.createControl(SoapFaultDetail.class);
        SoapFaultDetail faultDetailMock = (SoapFaultDetail) faultDetailControl.getMock();
        MockControl faultDetailElementControl = MockControl.createControl(SoapFaultDetailElement.class);
        SoapFaultDetailElement faultDetailElementMock = (SoapFaultDetailElement) faultDetailElementControl.getMock();

        final SoapMessage requestMock = (SoapMessage) messageControl.getMock();
        final SoapMessage responseMock = (SoapMessage) messageControl.getMock();
        SoapMessageContext soapMessageContext = new AbstractSoapMessageContext() {
            SoapMessage response = null;

            public SoapMessage getSoapRequest() {
                return requestMock;
            }

            public SoapMessage createSoapResponseInternal() {
                response = responseMock;
                return response;
            }

            public SoapMessage getSoapResponse() {
                return response;
            }
        };
        messageControl.expectAndReturn(requestMock.getPayloadSource(), new StringSource(INVALID_MESSAGE));
        messageControl.expectAndReturn(responseMock.getSoapBody(), bodyMock);
        messageControl.expectAndReturn(responseMock.getVersion(), SoapVersion.SOAP_11);
        bodyControl.expectAndReturn(bodyMock.addFault(SoapVersion.SOAP_11.getSenderFaultName(), "Validation error"),
                faultMock);
        faultControl.expectAndReturn(faultMock.addFaultDetail(), faultDetailMock);
        faultDetailControl.expectAndReturn(faultDetailMock.addFaultDetailElement(
                PayloadValidatingInterceptor.VALIDATION_ERROR_DETAIL_ELEMENT_NAME), faultDetailElementMock, 3);
        faultDetailElementMock.addText(null);
        faultDetailElementControl.setMatcher(MockControl.ALWAYS_MATCHER);
        faultDetailElementControl.setVoidCallable(3);

        messageControl.replay();
        bodyControl.replay();
        faultControl.replay();
        faultDetailControl.replay();
        faultDetailElementControl.replay();

        boolean result = interceptor.handleRequest(soapMessageContext, null);
        assertFalse("Invalid response from interceptor", result);

        messageControl.verify();
        bodyControl.verify();
        faultControl.verify();
        faultDetailControl.verify();
        faultDetailElementControl.verify();
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