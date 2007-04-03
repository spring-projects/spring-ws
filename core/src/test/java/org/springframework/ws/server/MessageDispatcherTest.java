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

package org.springframework.ws.server;

import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;

public class MessageDispatcherTest extends TestCase {

    private MessageDispatcher dispatcher;

    private MessageContext messageContext;

    private MockControl factoryControl;

    private WebServiceMessageFactory factoryMock;

    protected void setUp() throws Exception {
        dispatcher = new MessageDispatcher();
        factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        messageContext = new DefaultMessageContext(new MockWebServiceMessage(), factoryMock);
    }

    public void testGetEndpoint() throws Exception {
        MockControl mappingControl = MockControl.createControl(EndpointMapping.class);
        EndpointMapping mappingMock = (EndpointMapping) mappingControl.getMock();
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInvocationChain chain = new EndpointInvocationChain(new Object());

        mappingControl.expectAndReturn(mappingMock.getEndpoint(messageContext), chain);

        mappingControl.replay();
        factoryControl.replay();
        EndpointInvocationChain result = dispatcher.getEndpoint(messageContext);
        mappingControl.verify();
        factoryControl.verify();
        assertEquals("getEndpoint returns invalid EndpointInvocationChain", chain, result);
    }

    public void testGetEndpointAdapterSupportedEndpoint() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        adapterControl.expectAndReturn(adapterMock.supports(endpoint), true);
        adapterControl.replay();
        factoryControl.replay();
        EndpointAdapter result = dispatcher.getEndpointAdapter(endpoint);
        adapterControl.verify();
        factoryControl.verify();
        assertEquals("getEnpointAdapter returns invalid EndpointAdapter", adapterMock, result);
    }

    public void testGetEndpointAdapterUnsupportedEndpoint() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        adapterControl.expectAndReturn(adapterMock.supports(endpoint), false);
        adapterControl.replay();
        factoryControl.replay();
        try {
            dispatcher.getEndpointAdapter(endpoint);
            fail("getEndpointAdapter does not throw IllegalStateException for unsupported endpoint");
        }
        catch (IllegalStateException ex) {
            // Expected
        }
        adapterControl.verify();
        factoryControl.verify();
    }

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
                assertEquals("Invalid message context", messageContext, givenMessageContext);
                assertNull("Invalid endpoint", givenEndpoint);
                assertEquals("Invalid exception", ex, givenException);
                givenMessageContext.getResponse();
                return true;
            }

        };
        dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolver));
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), new MockWebServiceMessage());
        factoryControl.replay();

        dispatcher.dispatch(messageContext);
        assertNotNull("processEndpointException sets no response", messageContext.getResponse());
        factoryControl.verify();
    }

    public void testProcessUnsupportedEndpointException() throws Exception {
        MockControl resolverControl = MockControl.createControl(EndpointExceptionResolver.class);
        EndpointExceptionResolver resolverMock = (EndpointExceptionResolver) resolverControl.getMock();
        dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolverMock));

        Object endpoint = new Object();
        Exception ex = new Exception();

        resolverControl.expectAndReturn(resolverMock.resolveException(messageContext, endpoint, ex), false);

        resolverControl.replay();
        try {
            dispatcher.processEndpointException(messageContext, endpoint, ex);
        }
        catch (Exception result) {
            assertEquals("processEndpointException throws invalid exception", ex, result);
        }
        resolverControl.verify();
    }

    public void testNormalFlow() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        adapterControl.expectAndReturn(adapterMock.supports(endpoint), true);

        MockControl mappingControl = MockControl.createControl(EndpointMapping.class);
        EndpointMapping mappingMock = (EndpointMapping) mappingControl.getMock();
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        MockControl interceptorControl = MockControl.createStrictControl(EndpointInterceptor.class);
        EndpointInterceptor interceptorMock1 = (EndpointInterceptor) interceptorControl.getMock();
        EndpointInterceptor interceptorMock2 = (EndpointInterceptor) interceptorControl.getMock();

        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext, endpoint), true);
        interceptorControl.expectAndReturn(interceptorMock2.handleRequest(messageContext, endpoint), true);
        adapterMock.invoke(messageContext, endpoint);
        interceptorControl.expectAndReturn(interceptorMock2.handleResponse(messageContext, endpoint), true);
        interceptorControl.expectAndReturn(interceptorMock1.handleResponse(messageContext, endpoint), true);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});

        mappingControl.expectAndReturn(mappingMock.getEndpoint(messageContext), chain);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), new MockWebServiceMessage());

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();
        factoryControl.replay();
        //  response required for interceptor invocation
        messageContext.getResponse();
        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
        factoryControl.verify();
    }

    public void testFlowNoResponse() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        adapterControl.expectAndReturn(adapterMock.supports(endpoint), true);

        MockControl mappingControl = MockControl.createControl(EndpointMapping.class);
        EndpointMapping mappingMock = (EndpointMapping) mappingControl.getMock();
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        MockControl interceptorControl = MockControl.createStrictControl(EndpointInterceptor.class);
        EndpointInterceptor interceptorMock1 = (EndpointInterceptor) interceptorControl.getMock();
        EndpointInterceptor interceptorMock2 = (EndpointInterceptor) interceptorControl.getMock();

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});
        mappingControl.expectAndReturn(mappingMock.getEndpoint(messageContext), chain);

        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext, endpoint), true);
        interceptorControl.expectAndReturn(interceptorMock2.handleRequest(messageContext, endpoint), true);
        adapterMock.invoke(messageContext, endpoint);

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();
        factoryControl.replay();

        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
        factoryControl.verify();
    }

    public void testInterceptedRequestFlow() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        MockControl mappingControl = MockControl.createControl(EndpointMapping.class);
        EndpointMapping mappingMock = (EndpointMapping) mappingControl.getMock();
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        MockControl interceptorControl = MockControl.createStrictControl(EndpointInterceptor.class);
        EndpointInterceptor interceptorMock1 = (EndpointInterceptor) interceptorControl.getMock();
        EndpointInterceptor interceptorMock2 = (EndpointInterceptor) interceptorControl.getMock();

        Object endpoint = new Object();
        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext, endpoint), false);
        interceptorControl.expectAndReturn(interceptorMock1.handleResponse(messageContext, endpoint), true);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});

        mappingControl.expectAndReturn(mappingMock.getEndpoint(messageContext), chain);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), new MockWebServiceMessage());

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();
        factoryControl.replay();

        //  response required for interceptor invocation
        messageContext.getResponse();

        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
        factoryControl.verify();
    }

    public void testInterceptedResponseFlow() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        MockControl mappingControl = MockControl.createControl(EndpointMapping.class);
        EndpointMapping mappingMock = (EndpointMapping) mappingControl.getMock();
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        MockControl interceptorControl = MockControl.createStrictControl(EndpointInterceptor.class);
        EndpointInterceptor interceptorMock1 = (EndpointInterceptor) interceptorControl.getMock();
        EndpointInterceptor interceptorMock2 = (EndpointInterceptor) interceptorControl.getMock();

        Object endpoint = new Object();
        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext, endpoint), true);
        interceptorControl.expectAndReturn(interceptorMock2.handleRequest(messageContext, endpoint), false);
        interceptorControl.expectAndReturn(interceptorMock2.handleResponse(messageContext, endpoint), false);

        EndpointInvocationChain chain =
                new EndpointInvocationChain(endpoint, new EndpointInterceptor[]{interceptorMock1, interceptorMock2});

        mappingControl.expectAndReturn(mappingMock.getEndpoint(messageContext), chain);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), new MockWebServiceMessage());

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();
        factoryControl.replay();
        //  response required for interceptor invocation
        messageContext.getResponse();

        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
        factoryControl.verify();
    }

    public void testNoEndpointFound() throws Exception {
        dispatcher.setEndpointMappings(Collections.EMPTY_LIST);
        try {
            dispatcher.receive(messageContext);
            fail("NoEndpointFoundException expected");
        }
        catch (NoEndpointFoundException ex) {
            // expected
        }
    }

    public void testDetectStrategies() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", PayloadRootQNameEndpointMapping.class);
        applicationContext.registerSingleton("adapter", PayloadEndpointAdapter.class);
        applicationContext.registerSingleton("resolver", SimpleSoapExceptionResolver.class);
        dispatcher.setApplicationContext(applicationContext);
        assertEquals("Invalid amount of mappings detected", 1, dispatcher.getEndpointMappings().size());
        assertTrue("Invalid mappings detected",
                dispatcher.getEndpointMappings().get(0) instanceof PayloadRootQNameEndpointMapping);
        assertEquals("Invalid amount of adapters detected", 1, dispatcher.getEndpointAdapters().size());
        assertTrue("Invalid mappings detected",
                dispatcher.getEndpointAdapters().get(0) instanceof PayloadEndpointAdapter);
        assertEquals("Invalid amount of resolvers detected", 1, dispatcher.getEndpointExceptionResolvers().size());
        assertTrue("Invalid mappings detected",
                dispatcher.getEndpointExceptionResolvers().get(0) instanceof SimpleSoapExceptionResolver);
    }

    public void testDefaultStrategies() throws Exception {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        dispatcher.setApplicationContext(applicationContext);
    }

}