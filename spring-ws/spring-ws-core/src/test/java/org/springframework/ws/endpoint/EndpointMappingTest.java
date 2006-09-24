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

package org.springframework.ws.endpoint;

import junit.framework.TestCase;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.EndpointInvocationChain;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.mock.MockWebServiceMessage;

public class EndpointMappingTest extends TestCase {

    public void testDefaultEndpoint() throws Exception {
        final MockWebServiceMessage request = new MockWebServiceMessage();
        Object defaultEndpoint = new Object();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            protected Object getEndpointInternal(WebServiceMessage givenRequest) throws Exception {
                assertEquals("Invalid request passed", request, givenRequest);
                return null;
            }
        };
        mapping.setDefaultEndpoint(defaultEndpoint);

        EndpointInvocationChain result = mapping.getEndpoint(request);
        assertNotNull("No EndpointInvocatioChain returned", result);
        assertEquals("Default Endpoint not returned", defaultEndpoint, result.getEndpoint());
    }

    public void testEndpoint() throws Exception {
        final MockWebServiceMessage request = new MockWebServiceMessage();
        final Object endpoint = new Object();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            protected Object getEndpointInternal(WebServiceMessage givenRequest) throws Exception {
                assertEquals("Invalid request passed", request, givenRequest);
                return endpoint;
            }
        };

        EndpointInvocationChain result = mapping.getEndpoint(request);
        assertNotNull("No EndpointInvocatioChain returned", result);
        assertEquals("Unexpected Endpoint returned", endpoint, result.getEndpoint());
    }

    public void testEndpointInterceptors() throws Exception {
        final MockWebServiceMessage request = new MockWebServiceMessage();
        final Object endpoint = new Object();
        EndpointInterceptor interceptor = new EndpointInterceptorAdapter();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            protected Object getEndpointInternal(WebServiceMessage givenRequest) throws Exception {
                assertEquals("Invalid request passed", request, givenRequest);
                return endpoint;
            }
        };
        mapping.setInterceptors(new EndpointInterceptor[]{interceptor});
        EndpointInvocationChain result = mapping.getEndpoint(request);
        assertEquals("Unexpected amount of EndpointInterceptors returned", 1, result.getInterceptors().length);
        assertEquals("Unexpected EndpointInterceptor returned", interceptor, result.getInterceptors()[0]);
    }

    public void testEndpointBeanName() throws Exception {
        final MockWebServiceMessage request = new MockWebServiceMessage();

        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("endpoint", Object.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            protected Object getEndpointInternal(WebServiceMessage message) throws Exception {
                assertEquals("Invalid request", request, message);
                return "endpoint";
            }
        };
        mapping.setApplicationContext(applicationContext);

        EndpointInvocationChain result = mapping.getEndpoint(request);
        assertNotNull("No endpoint returned", result);
    }

    public void testEndpointInvalidBeanName() throws Exception {
        final MockWebServiceMessage request = new MockWebServiceMessage();

        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("endpoint", Object.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            protected Object getEndpointInternal(WebServiceMessage message) throws Exception {
                assertEquals("Invalid request", request, message);
                return "noSuchBean";
            }
        };
        mapping.setApplicationContext(applicationContext);

        EndpointInvocationChain result = mapping.getEndpoint(request);

        assertNull("No endpoint returned", result);
    }


}
