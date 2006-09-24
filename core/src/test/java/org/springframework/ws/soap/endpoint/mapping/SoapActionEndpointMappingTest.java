/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap.endpoint.mapping;

import junit.framework.TestCase;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockTransportContext;
import org.springframework.ws.mock.MockTransportRequest;
import org.springframework.ws.mock.MockWebServiceMessage;

public class SoapActionEndpointMappingTest extends TestCase {

    private SoapActionEndpointMapping mapping;

    private MockTransportRequest request;

    private MockMessageContext context;

    protected void setUp() throws Exception {
        request = new MockTransportRequest();
        MockTransportContext transportContext = new MockTransportContext(request);
        context = new MockMessageContext(new MockWebServiceMessage(), transportContext);
        mapping = new SoapActionEndpointMapping();
    }

    public void testGetLookupKeyForMessage() throws Exception {
        String soapAction = "http://springframework.org/spring-ws/SoapAction";
        request.addHeader(SoapActionEndpointMapping.SOAP_ACTION_HEADER, soapAction);
        assertEquals("Invalid lookup key", soapAction, mapping.getLookupKeyForMessage(context));
    }

    public void testGetLookupKeyForMessageQuoted() throws Exception {
        String soapAction = "http://springframework.org/spring-ws/SoapAction";
        request.addHeader(SoapActionEndpointMapping.SOAP_ACTION_HEADER, "\"" + soapAction + "\"");
        assertEquals("Invalid lookup key", soapAction, mapping.getLookupKeyForMessage(context));
    }

    public void testValidateLookupKey() throws Exception {
        mapping.validateLookupKey("SoapAction");
    }
}