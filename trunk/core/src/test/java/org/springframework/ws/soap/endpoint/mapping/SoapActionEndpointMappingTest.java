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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.DefaultTransportContext;
import org.springframework.ws.transport.StubTransportInputStream;
import org.springframework.ws.transport.StubTransportOutputStream;
import org.springframework.ws.transport.TransportContext;
import org.springframework.ws.transport.TransportContextHolder;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;

public class SoapActionEndpointMappingTest extends TestCase {

    private SoapActionEndpointMapping mapping;

    private MessageContext context;

    private Map headers;

    protected void setUp() throws Exception {
        headers = new HashMap();
        TransportInputStream tis = new StubTransportInputStream(new ByteArrayInputStream(new byte[0]), headers);
        TransportOutputStream tos = new StubTransportOutputStream(new ByteArrayOutputStream());
        TransportContext transportContext = new DefaultTransportContext(tis, tos);
        TransportContextHolder.setTransportContext(transportContext);
        mapping = new SoapActionEndpointMapping();
        context = new DefaultMessageContext(new MockWebServiceMessageFactory());
    }

    protected void tearDown() throws Exception {
        TransportContextHolder.setTransportContext(null);
    }

    public void testGetLookupKeyForMessage() throws Exception {
        String soapAction = "http://springframework.org/spring-ws/SoapAction";
        headers.put(SoapActionEndpointMapping.SOAP_ACTION_HEADER, soapAction);
        assertEquals("Invalid lookup key", soapAction, mapping.getLookupKeyForMessage(context));
    }

    public void testGetLookupKeyForMessageQuoted() throws Exception {
        String soapAction = "http://springframework.org/spring-ws/SoapAction";
        headers.put(SoapActionEndpointMapping.SOAP_ACTION_HEADER, "\"" + soapAction + "\"");
        assertEquals("Invalid lookup key", soapAction, mapping.getLookupKeyForMessage(context));
    }

    public void testValidateLookupKey() throws Exception {
        mapping.validateLookupKey("SoapAction");
    }
}