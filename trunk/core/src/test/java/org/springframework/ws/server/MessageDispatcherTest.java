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

package org.springframework.ws.server;

import java.util.Collections;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class MessageDispatcherTest {

    private MessageDispatcher dispatcher;

    private MessageContext messageContext;

    private WebServiceMessageFactory factoryMock;

    @Before
    public void setUp() throws Exception {
        dispatcher = new MessageDispatcher();
        factoryMock = createMock(WebServiceMessageFactory.class);
        messageContext = new DefaultMessageContext(new MockWebServiceMessage(), factoryMock);
    }

    @Test
    public void testGetEndpoint() throws Exception {
        EndpointMapping mappingMock = createMock(EndpointMapping.class);
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInvocationChain chain = new EndpointInvocationChain(new Object());

        expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);

        replay(mappingMock, factoryMock);

        EndpointInvocationChain result = dispatcher.getEndpoint(messageContext);

        verify(mappingMock, factoryMock);

        Assert.assertEquals("getEndpoint returns invalid EndpointInvocationChain", chain, result);
    }

    @Test
    public void testGetEndpointAdapterSupportedEndpoint() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        expect(adapterMock.supports(endpoint)).andReturn(true);

        replay(adapterMock, factoryMock);

        EndpointAdapter result = dispatcher.getEndpointAdapter(endpoint);

        verify(adapterMock, factoryMock);

        Assert.assertEquals("getEnpointAdapter returns invalid EndpointAdapter", adapterMock, result);
    }

    @Test
    public void testGetEndpointAdapterUnsupportedEndpoint() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        expect(adapterMock.supports(endpoint)).andReturn(false);

        replay(adapterMock, factoryMock);

        try {
            dispatcher.getEndpointAdapter(endpoint);
            Assert.fail("getEndpointAdapter does not throw IllegalStateException for unsupported endpoint");
        }
        catch (IllegalStateException ex) {
            // Expected
        }

        verify(adapterMock, factoryMock);
    }

    @Test
    public void testResolveException() throws Exception {
        final Exception ex = new Exception();
        EndpointMapping endpointMapping = new EndpointMapping() {

            public EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception {
                throw ex;
            }
        };
        dispatcher.setEndpointMappings(Collections.singletonList(endpointMapping));
        EndpointExceptionResolver resolver = new EndpointExceptionResolver() {

            public boolean resolveException(MessageContext givenMessageContext,
                                            Object givenEndpoint,
                                            Exception givenException) {
                Assert.assertEquals("Invalid message context", messageContext, givenMessageContext);
                Assert.assertNull("Invalid endpoint", givenEndpoint);
                Assert.assertEquals("Invalid exception", ex, givenException);
                givenMessageContext.getResponse();
                return true;
            }

        };
        dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolver));
        expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

        replay(factoryMock);

        dispatcher.dispatch(messageContext);
        Assert.assertNotNull("processEndpointException sets no response", messageContext.getResponse());

        verify(factoryMock);
    }

    @Test
    public void testProcessUnsupportedEndpointException() throws Exception {
        EndpointExceptionResolver resolverMock = createMock(EndpointExceptionResolver.class);
        dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolverMock));

        Object endpoint = new Object();
        Exception ex = new Exception();

        expect(resolverMock.resolveException(messageContext, endpoint, ex)).andReturn(false);

        replay(factoryMock, resolverMock);

        try {
            dispatcher.processEndpointException(messageContext, endpoint, ex);
        }
        catch (Exception result) {
            Assert.assertEquals("processEndpointException throws invalid exception", ex, result);
        }
        verify(factoryMock, resolverMock);
    }

    @Test
    public void testNormalFlow() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        expect(adapterMock.supports(endpoint)).andReturn(true);

        EndpointMapping mappingMock = createMock(EndpointMapping.class);
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInterceptor interceptorMock1 = createMock("interceptor1", EndpointInterceptor.class);
        EndpointInterceptor interceptorMock2 = createMock("interceptor2", EndpointInterceptor.class);

        expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(true);
        expect(interceptorMock2.handleRequest(messageContext, endpoint)).andReturn(true);

        adapterMock.invoke(messageContext, endpoint);

        expect(interceptorMock2.handleResponse(messageContext, endpoint)).andReturn(true);
        expect(interceptorMock1.handleResponse(messageContext, endpoint)).andReturn(true);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});

        expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
        expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

        replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

        //  response required for interceptor invocation
        messageContext.getResponse();
        dispatcher.dispatch(messageContext);

        verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);
    }

    @Test
    public void testFlowNoResponse() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        expect(adapterMock.supports(endpoint)).andReturn(true);

        EndpointMapping mappingMock = createMock(EndpointMapping.class);
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInterceptor interceptorMock1 = createMock("interceptor1", EndpointInterceptor.class);
        EndpointInterceptor interceptorMock2 = createMock("interceptor2", EndpointInterceptor.class);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});
        expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);

        expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(true);
        expect(interceptorMock2.handleRequest(messageContext, endpoint)).andReturn(true);

        adapterMock.invoke(messageContext, endpoint);

        replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

        dispatcher.dispatch(messageContext);

        verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);
    }

    @Test
    public void testInterceptedRequestFlow() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        EndpointMapping mappingMock = createMock(EndpointMapping.class);
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInterceptor interceptorMock1 = createMock("interceptor1", EndpointInterceptor.class);
        EndpointInterceptor interceptorMock2 = createMock("interceptor2", EndpointInterceptor.class);

        Object endpoint = new Object();

        expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(false);
        expect(interceptorMock1.handleResponse(messageContext, endpoint)).andReturn(true);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});

        expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
        expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

        replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

        //  response required for interceptor invocation
        messageContext.getResponse();

        dispatcher.dispatch(messageContext);

        verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);
    }

    @Test
    public void testInterceptedResponseFlow() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        EndpointMapping mappingMock = createMock(EndpointMapping.class);
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInterceptor interceptorMock1 = createMock("interceptor1", EndpointInterceptor.class);
        EndpointInterceptor interceptorMock2 = createMock("interceptor2", EndpointInterceptor.class);

        Object endpoint = new Object();
        expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(true);
        expect(interceptorMock2.handleRequest(messageContext, endpoint)).andReturn(false);
        expect(interceptorMock2.handleResponse(messageContext, endpoint)).andReturn(false);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});

        expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
        expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

        replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

        //  response required for interceptor invocation
        messageContext.getResponse();

        dispatcher.dispatch(messageContext);

        verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);
    }

    @Test
    public void testFaultFlow() throws Exception {
        EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        expect(adapterMock.supports(endpoint)).andReturn(true);

        EndpointMapping mappingMock = createMock(EndpointMapping.class);
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInterceptor interceptorMock = createMock(EndpointInterceptor.class);

        expect(interceptorMock.handleRequest(messageContext, endpoint)).andReturn(true);
        adapterMock.invoke(messageContext, endpoint);
        expect(interceptorMock.handleFault(messageContext, endpoint)).andReturn(true);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock});

        expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
        MockWebServiceMessage response = new MockWebServiceMessage();
        response.setFault(true);
        expect(factoryMock.createWebServiceMessage()).andReturn(response);

        replay(mappingMock, interceptorMock, adapterMock, factoryMock);

        //  response required for interceptor invocation
        messageContext.getResponse();
        dispatcher.dispatch(messageContext);

        verify(mappingMock, interceptorMock, adapterMock, factoryMock);
    }

    @Test
    public void testNoEndpointFound() throws Exception {
        dispatcher.setEndpointMappings(Collections.<EndpointMapping>emptyList());
        try {
            dispatcher.receive(messageContext);
            Assert.fail("NoEndpointFoundException expected");
        }
        catch (NoEndpointFoundException ex) {
            // expected
        }
    }

    @Test
    public void testDetectStrategies() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", PayloadRootQNameEndpointMapping.class);
        applicationContext.registerSingleton("adapter", PayloadEndpointAdapter.class);
        applicationContext.registerSingleton("resolver", SimpleSoapExceptionResolver.class);
        dispatcher.setApplicationContext(applicationContext);
        Assert.assertEquals("Invalid amount of mappings detected", 1, dispatcher.getEndpointMappings().size());
        Assert.assertTrue("Invalid mappings detected",
                dispatcher.getEndpointMappings().get(0) instanceof PayloadRootQNameEndpointMapping);
        Assert.assertEquals("Invalid amount of adapters detected", 1, dispatcher.getEndpointAdapters().size());
        Assert.assertTrue("Invalid mappings detected",
                dispatcher.getEndpointAdapters().get(0) instanceof PayloadEndpointAdapter);
        Assert.assertEquals("Invalid amount of resolvers detected", 1,
                dispatcher.getEndpointExceptionResolvers().size());
        Assert.assertTrue("Invalid mappings detected",
                dispatcher.getEndpointExceptionResolvers().get(0) instanceof SimpleSoapExceptionResolver);
    }

    @Test
    public void testDefaultStrategies() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        dispatcher.setApplicationContext(applicationContext);
    }

}