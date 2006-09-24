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

package org.springframework.ws;

import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockWebServiceMessage;

public class MessageDispatcherTest extends TestCase {

    private MessageDispatcher dispatcher;

    private MockMessageContext messageContext;

    private MockWebServiceMessage request;

    protected void setUp() throws Exception {
        dispatcher = new MessageDispatcher();
        dispatcher.setApplicationContext(new StaticApplicationContext());
        dispatcher.afterPropertiesSet();
        request = new MockWebServiceMessage();
        messageContext = new MockMessageContext(request);
    }

    public void testGetEndpoint() throws Exception {
        MockControl mappingControl = MockControl.createControl(EndpointMapping.class);
        EndpointMapping mappingMock = (EndpointMapping) mappingControl.getMock();
        dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

        EndpointInvocationChain chain = new EndpointInvocationChain(new Object());

        mappingControl.expectAndReturn(mappingMock.getEndpoint(request), chain);

        mappingControl.replay();
        EndpointInvocationChain result = dispatcher.getEndpoint(request);
        mappingControl.verify();
        assertEquals("getEndpoint returns invalid EndpointInvocationChain", chain, result);
    }

    public void testGetEndpointAdapterSupportedEndpoint() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        adapterControl.expectAndReturn(adapterMock.supports(endpoint), true);
        adapterControl.replay();
        EndpointAdapter result = dispatcher.getEndpointAdapter(endpoint);
        adapterControl.verify();
        assertEquals("getEnpointAdapter returns invalid EndpointAdapter", adapterMock, result);
    }

    public void testGetEndpointAdapterUnsupportedEndpoint() throws Exception {
        MockControl adapterControl = MockControl.createControl(EndpointAdapter.class);
        EndpointAdapter adapterMock = (EndpointAdapter) adapterControl.getMock();
        dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

        Object endpoint = new Object();
        adapterControl.expectAndReturn(adapterMock.supports(endpoint), false);
        adapterControl.replay();
        try {
            dispatcher.getEndpointAdapter(endpoint);
            fail("getEndpointAdapter does not throw IllegalStateException for unsupported endpoint");
        }
        catch (IllegalStateException ex) {
            // Expected
        }
        adapterControl.verify();
    }

    public void testProcessEndpointExceptionReturnsResponse() throws Exception {

        final Object endpoint = new Object();
        final Exception ex = new Exception();
        EndpointExceptionResolver resolver = new EndpointExceptionResolver() {

            public boolean resolveException(MessageContext givenMessageContext,
                                            Object givenEndpoint,
                                            Exception givenException) {
                assertEquals("Invalid message context", messageContext, givenMessageContext);
                assertEquals("Invalid endpoint", endpoint, givenEndpoint);
                assertEquals("Invalid exception", ex, givenException);
                givenMessageContext.createResponse();
                return true;
            }

        };
        dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolver));

        dispatcher.processEndpointException(messageContext, endpoint, ex);
        assertNotNull("processEndpointException sets no response", messageContext.getResponse());
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

        mappingControl.expectAndReturn(mappingMock.getEndpoint(request), chain);

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();
        //  response required for interceptor invocation
        messageContext.createResponse();
        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
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
        mappingControl.expectAndReturn(mappingMock.getEndpoint(request), chain);

        interceptorControl.expectAndReturn(interceptorMock1.handleRequest(messageContext, endpoint), true);
        interceptorControl.expectAndReturn(interceptorMock2.handleRequest(messageContext, endpoint), true);
        adapterMock.invoke(messageContext, endpoint);

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();

        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
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

        mappingControl.expectAndReturn(mappingMock.getEndpoint(request), chain);

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();

        //  response required for interceptor invocation
        messageContext.createResponse();

        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
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

        mappingControl.expectAndReturn(mappingMock.getEndpoint(request), chain);

        mappingControl.replay();
        interceptorControl.replay();
        adapterControl.replay();
        //  response required for interceptor invocation
        messageContext.createResponse();

        dispatcher.dispatch(messageContext);

        mappingControl.verify();
        interceptorControl.verify();
        adapterControl.verify();
    }

}