/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class EndpointMappingTest {

    private MessageContext mockContext;

    @Before
    public void setUp() throws Exception {
        mockContext = createMock(MessageContext.class);
    }

    @Test
    public void testDefaultEndpoint() throws Exception {
        Object defaultEndpoint = new Object();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            @Override
            protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
                Assert.assertEquals("Invalid request passed", mockContext, givenRequest);
                return null;
            }
        };
        mapping.setDefaultEndpoint(defaultEndpoint);

        replay(mockContext);

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        Assert.assertNotNull("No EndpointInvocatioChain returned", result);
        Assert.assertEquals("Default Endpoint not returned", defaultEndpoint, result.getEndpoint());

        verify(mockContext);
    }

    @Test
    public void testEndpoint() throws Exception {
        final Object endpoint = new Object();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            @Override
            protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
                Assert.assertEquals("Invalid request passed", mockContext, givenRequest);
                return endpoint;
            }
        };
        replay(mockContext);

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        Assert.assertNotNull("No EndpointInvocatioChain returned", result);
        Assert.assertEquals("Unexpected Endpoint returned", endpoint, result.getEndpoint());

        verify(mockContext);
    }

    @Test
    public void testEndpointInterceptors() throws Exception {
        final Object endpoint = new Object();
        EndpointInterceptor interceptor = new EndpointInterceptorAdapter();
        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
            @Override
            protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
                Assert.assertEquals("Invalid request passed", mockContext, givenRequest);
                return endpoint;
            }
        };

        replay(mockContext);

        mapping.setInterceptors(new EndpointInterceptor[]{interceptor});
        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        Assert.assertEquals("Unexpected amount of EndpointInterceptors returned", 1, result.getInterceptors().length);
        Assert.assertEquals("Unexpected EndpointInterceptor returned", interceptor, result.getInterceptors()[0]);

        verify(mockContext);
    }

    @Test
    public void testEndpointBeanName() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("endpoint", Object.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            @Override
            protected Object getEndpointInternal(MessageContext message) throws Exception {
                Assert.assertEquals("Invalid request", mockContext, message);
                return "endpoint";
            }
        };
        mapping.setApplicationContext(applicationContext);

        replay(mockContext);

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        Assert.assertNotNull("No endpoint returned", result);

        verify(mockContext);
    }

    @Test
    public void testEndpointInvalidBeanName() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("endpoint", Object.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            @Override
            protected Object getEndpointInternal(MessageContext message) throws Exception {
                Assert.assertEquals("Invalid request", mockContext, message);
                return "noSuchBean";
            }
        };
        mapping.setApplicationContext(applicationContext);

        replay(mockContext);

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);

        Assert.assertNull("No endpoint returned", result);

        verify(mockContext);
    }

    @Test
    public void testEndpointPrototype() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerPrototype("endpoint", MyEndpoint.class);

        AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

            @Override
            protected Object getEndpointInternal(MessageContext message) throws Exception {
                Assert.assertEquals("Invalid request", mockContext, message);
                return "endpoint";
            }
        };
        mapping.setApplicationContext(applicationContext);

        replay(mockContext);

        EndpointInvocationChain result = mapping.getEndpoint(mockContext);
        Assert.assertNotNull("No endpoint returned", result);
        result = mapping.getEndpoint(mockContext);
        Assert.assertNotNull("No endpoint returned", result);
        Assert.assertEquals("Prototype endpoint was not constructed twice", 2, MyEndpoint.constrCount);

        verify(mockContext);
    }

    private static class MyEndpoint {

        private static int constrCount;

        private MyEndpoint() {
            constrCount++;
        }
    }

}
