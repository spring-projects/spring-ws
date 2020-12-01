/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter;
import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping;
import org.springframework.ws.soap.server.endpoint.SimpleSoapExceptionResolver;

public class MessageDispatcherTest {

	private MessageDispatcher dispatcher;

	private MessageContext messageContext;

	private WebServiceMessageFactory factoryMock;

	@BeforeEach
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

		assertThat(result).isEqualTo(chain);
	}

	@Test
	public void testGetEndpointAdapterSupportedEndpoint() {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		replay(adapterMock, factoryMock);

		EndpointAdapter result = dispatcher.getEndpointAdapter(endpoint);

		verify(adapterMock, factoryMock);

		assertThat(result).isEqualTo(adapterMock);
	}

	@Test
	public void testGetEndpointAdapterUnsupportedEndpoint() {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(false);

		replay(adapterMock, factoryMock);

		assertThatIllegalStateException().isThrownBy(() -> dispatcher.getEndpointAdapter(endpoint));

		verify(adapterMock, factoryMock);
	}

	@Test
	public void testResolveException() throws Exception {

		final Exception ex = new Exception();

		EndpointMapping endpointMapping = messageContext -> {
			throw ex;
		};

		dispatcher.setEndpointMappings(Collections.singletonList(endpointMapping));

		EndpointExceptionResolver resolver = (givenMessageContext, givenEndpoint, givenException) -> {

			assertThat(givenMessageContext).isEqualTo(messageContext);
			assertThat(givenEndpoint).isNull();
			assertThat(givenException).isEqualTo(ex);

			givenMessageContext.getResponse();
			return true;
		};

		dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolver));
		expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(factoryMock);

		dispatcher.dispatch(messageContext);

		assertThat(messageContext.getResponse()).isNotNull();

		verify(factoryMock);
	}

	@Test
	public void testProcessUnsupportedEndpointException() {

		EndpointExceptionResolver resolverMock = createMock(EndpointExceptionResolver.class);
		dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolverMock));

		Object endpoint = new Object();
		Exception ex = new Exception();

		expect(resolverMock.resolveException(messageContext, endpoint, ex)).andReturn(false);

		replay(factoryMock, resolverMock);

		try {
			dispatcher.processEndpointException(messageContext, endpoint, ex);
		} catch (Exception result) {
			assertThat(result).isEqualTo(ex);
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

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(true);
		expect(interceptorMock2.handleRequest(messageContext, endpoint)).andReturn(true);

		adapterMock.invoke(messageContext, endpoint);

		expect(interceptorMock2.handleResponse(messageContext, endpoint)).andReturn(true);
		expect(interceptorMock1.handleResponse(messageContext, endpoint)).andReturn(true);

		interceptorMock2.afterCompletion(messageContext, endpoint, null);
		interceptorMock1.afterCompletion(messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });

		expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
		expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

		// response required for interceptor invocation
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

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });
		expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);

		expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(true);
		expect(interceptorMock2.handleRequest(messageContext, endpoint)).andReturn(true);
		interceptorMock2.afterCompletion(messageContext, endpoint, null);
		interceptorMock1.afterCompletion(messageContext, endpoint, null);

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

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		Object endpoint = new Object();

		expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(false);
		expect(interceptorMock1.handleResponse(messageContext, endpoint)).andReturn(true);
		interceptorMock1.afterCompletion(messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });

		expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
		expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

		// response required for interceptor invocation
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

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		Object endpoint = new Object();
		expect(interceptorMock1.handleRequest(messageContext, endpoint)).andReturn(true);
		expect(interceptorMock2.handleRequest(messageContext, endpoint)).andReturn(false);
		expect(interceptorMock2.handleResponse(messageContext, endpoint)).andReturn(false);
		interceptorMock1.afterCompletion(messageContext, endpoint, null);
		interceptorMock2.afterCompletion(messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });

		expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
		expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);

		// response required for interceptor invocation
		messageContext.getResponse();

		dispatcher.dispatch(messageContext);

		verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, factoryMock);
	}

	@Test
	public void testResolveExceptionsWithInterceptors() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointExceptionResolver resolverMock = createMock(EndpointExceptionResolver.class);
		dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolverMock));

		EndpointInterceptor interceptorMock = createStrictMock("interceptor1", EndpointInterceptor.class);

		expect(interceptorMock.handleRequest(messageContext, endpoint)).andReturn(true);

		adapterMock.invoke(messageContext, endpoint);
		RuntimeException exception = new RuntimeException();
		expectLastCall().andThrow(exception);

		expect(resolverMock.resolveException(messageContext, endpoint, exception)).andReturn(true);

		expect(interceptorMock.handleResponse(messageContext, endpoint)).andReturn(true);

		interceptorMock.afterCompletion(messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock });

		expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
		expect(factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock, adapterMock, factoryMock, resolverMock);

		// response required for interceptor invocation
		messageContext.getResponse();
		try {
			dispatcher.dispatch(messageContext);
		} catch (RuntimeException ex) {

		}

		verify(mappingMock, interceptorMock, adapterMock, factoryMock, resolverMock);
	}

	@Test
	public void testFaultFlow() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInterceptor interceptorMock = createStrictMock(EndpointInterceptor.class);

		expect(interceptorMock.handleRequest(messageContext, endpoint)).andReturn(true);
		adapterMock.invoke(messageContext, endpoint);
		expect(interceptorMock.handleFault(messageContext, endpoint)).andReturn(true);
		interceptorMock.afterCompletion(messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock });

		expect(mappingMock.getEndpoint(messageContext)).andReturn(chain);
		MockWebServiceMessage response = new MockWebServiceMessage();
		response.setFault(true);
		expect(factoryMock.createWebServiceMessage()).andReturn(response);

		replay(mappingMock, interceptorMock, adapterMock, factoryMock);

		// response required for interceptor invocation
		messageContext.getResponse();
		dispatcher.dispatch(messageContext);

		verify(mappingMock, interceptorMock, adapterMock, factoryMock);
	}

	@Test
	public void testNoEndpointFound() throws Exception {

		dispatcher.setEndpointMappings(Collections.emptyList());

		assertThatExceptionOfType(NoEndpointFoundException.class).isThrownBy(() -> dispatcher.receive(messageContext));
	}

	@Test
	public void testDetectStrategies() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();

		applicationContext.registerSingleton("mapping", PayloadRootQNameEndpointMapping.class);
		applicationContext.registerSingleton("adapter", PayloadEndpointAdapter.class);
		applicationContext.registerSingleton("resolver", SimpleSoapExceptionResolver.class);

		dispatcher.setApplicationContext(applicationContext);

		assertThat(dispatcher.getEndpointMappings()).hasSize(1);
		assertThat(dispatcher.getEndpointMappings()).hasOnlyElementsOfType(PayloadRootQNameEndpointMapping.class);

		assertThat(dispatcher.getEndpointAdapters()).hasSize(1);
		assertThat(dispatcher.getEndpointAdapters()).hasOnlyElementsOfTypes(PayloadEndpointAdapter.class);

		assertThat(dispatcher.getEndpointExceptionResolvers()).hasSize(1);
		assertThat(dispatcher.getEndpointExceptionResolvers()).hasOnlyElementsOfType(SimpleSoapExceptionResolver.class);
	}

	@Test
	public void testDefaultStrategies() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		dispatcher.setApplicationContext(applicationContext);
	}

}
