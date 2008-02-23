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

package org.springframework.ws.soap.addressing.server;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.addressing.AbstractWsAddressingTestCase;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;

public class SimpleActionEndpointMappingTest extends AbstractWsAddressingTestCase {

    private SimpleActionEndpointMapping mapping;

    private Endpoint1 endpoint1;

    protected void onSetUp() throws Exception {
        mapping = new SimpleActionEndpointMapping();
        Map map = new HashMap();
        endpoint1 = new Endpoint1();
        Endpoint2 endpoint2 = new Endpoint2();
        map.put("http://fabrikam123.example/mail/Delete", endpoint1);
        map.put("http://fabrikam123.example/mail/Add", endpoint2);
        mapping.setPreInterceptors(new EndpointInterceptor[]{new PayloadLoggingInterceptor()});
        mapping.setPostInterceptors(new EndpointInterceptor[]{new PayloadValidatingInterceptor()});
        mapping.setAddress(new URI("mailto:joe@fabrikam123.example"));
        mapping.setActionMap(map);
        mapping.afterPropertiesSet();
    }

    public void testMatch() throws Exception {
        SaajSoapMessage message = loadSaajMessage("200408/valid.xml");
        MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

        EndpointInvocationChain endpoint = mapping.getEndpoint(messageContext);
        assertNotNull("No endpoint returned", endpoint);
        assertEquals("Invalid endpoint returned", endpoint1, endpoint.getEndpoint());
        EndpointInterceptor[] interceptors = endpoint.getInterceptors();
        assertEquals("Invalid amount of interceptors returned", 3, interceptors.length);
        assertTrue("Invalid first interceptor", interceptors[0] instanceof PayloadLoggingInterceptor);
        assertTrue("Invalid first interceptor", interceptors[1] instanceof AddressingEndpointInterceptor);
        assertTrue("Invalid first interceptor", interceptors[2] instanceof PayloadValidatingInterceptor);
    }

    public void testNoMatch() throws Exception {
        SaajSoapMessage message = loadSaajMessage("200408/response-no-message-id.xml");
        MessageContext messageContext = new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));

        EndpointInvocationChain endpoint = mapping.getEndpoint(messageContext);
        assertNull("Endpoint returned", endpoint);
    }

    private static class Endpoint1 {

    }

    private static class Endpoint2 {

    }
}