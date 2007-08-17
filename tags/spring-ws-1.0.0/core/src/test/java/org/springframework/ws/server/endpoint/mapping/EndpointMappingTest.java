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

package org.springframework.ws.server.endpoint.mapping;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;

public class EndpointMappingTest extends TestCase {

    private MessageContext mockContext;

    private MockControl contextControl;

    protected void setUp() throws Exception {
        contextControl = MockControl.createControl(MessageContext.class);
        mockContext = (MessageContext) contextControl.getMock();
    }

    public void testDefaultEndpoint() throws Exception {
        Object defaultEndpoint = new Object();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
                assertEquals("Invalid request passed", mockContext, givenRequest);
                return null;
            }
        };
        mapping.setDefaultEndpoint(defaultEndpoint);
        contextControl.replay();

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        assertNotNull("No EndpointInvocatioChain returned", result);
        assertEquals("Default Endpoint not returned", defaultEndpoint, result.getEndpoint());
        contextControl.verify();
    }

    public void testEndpoint() throws Exception {
        final Object endpoint = new Object();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
                assertEquals("Invalid request passed", mockContext, givenRequest);
                return endpoint;
            }
        };
        contextControl.replay();

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        assertNotNull("No EndpointInvocatioChain returned", result);
        assertEquals("Unexpected Endpoint returned", endpoint, result.getEndpoint());
        contextControl.verify();
    }

    public void testEndpointInterceptors() throws Exception {
        final Object endpoint = new Object();
        EndpointInterceptor interceptor = new EndpointInterceptorAdapter();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
                assertEquals("Invalid request passed", mockContext, givenRequest);
                return endpoint;
            }
        };
        contextControl.replay();
        mapping.setInterceptors(new EndpointInterceptor[]{interceptor});
        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        assertEquals("Unexpected amount of EndpointInterceptors returned", 1, result.getInterceptors().length);
        assertEquals("Unexpected EndpointInterceptor returned", interceptor, result.getInterceptors()[0]);
        contextControl.verify();
    }

    public void testEndpointBeanName() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("endpoint", Object.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            protected Object getEndpointInternal(MessageContext message) throws Exception {
                assertEquals("Invalid request", mockContext, message);
                return "endpoint";
            }
        };
        mapping.setApplicationContext(applicationContext);
        contextControl.replay();

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        assertNotNull("No endpoint returned", result);
        contextControl.verify();
    }

    public void testEndpointInvalidBeanName() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("endpoint", Object.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            protected Object getEndpointInternal(MessageContext message) throws Exception {
                assertEquals("Invalid request", mockContext, message);
                return "noSuchBean";
            }
        };
        mapping.setApplicationContext(applicationContext);
        contextControl.replay();

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);

        assertNull("No endpoint returned", result);
        contextControl.verify();
    }


}
