/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class MessageDispatcherTests {

	private MessageDispatcher dispatcher;

	private MessageContext messageContext;

	private WebServiceMessageFactory factoryMock;

	@BeforeEach
	void setUp() {

		this.dispatcher = new MessageDispatcher();
		this.factoryMock = createMock(WebServiceMessageFactory.class);
		this.messageContext = new DefaultMessageContext(new MockWebServiceMessage(), this.factoryMock);
	}

	@Test
	void testGetEndpoint() throws Exception {

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInvocationChain chain = new EndpointInvocationChain(new Object());

		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);

		replay(mappingMock, this.factoryMock);

		EndpointInvocationChain result = this.dispatcher.getEndpoint(this.messageContext);

		verify(mappingMock, this.factoryMock);

		assertThat(result).isEqualTo(chain);
	}

	@Test
	void testGetEndpointAdapterSupportedEndpoint() {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		replay(adapterMock, this.factoryMock);

		EndpointAdapter result = this.dispatcher.getEndpointAdapter(endpoint);

		verify(adapterMock, this.factoryMock);

		assertThat(result).isEqualTo(adapterMock);
	}

	@Test
	void testGetEndpointAdapterUnsupportedEndpoint() {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(false);

		replay(adapterMock, this.factoryMock);

		assertThatIllegalStateException().isThrownBy(() -> this.dispatcher.getEndpointAdapter(endpoint));

		verify(adapterMock, this.factoryMock);
	}

	@Test
	void testResolveException() throws Exception {

		final Exception ex = new Exception();

		EndpointMapping endpointMapping = messageContext -> {
			throw ex;
		};

		this.dispatcher.setEndpointMappings(Collections.singletonList(endpointMapping));

		EndpointExceptionResolver resolver = (givenMessageContext, givenEndpoint, givenException) -> {

			assertThat(givenMessageContext).isEqualTo(this.messageContext);
			assertThat(givenEndpoint).isNull();
			assertThat(givenException).isEqualTo(ex);

			givenMessageContext.getResponse();
			return true;
		};

		this.dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolver));
		expect(this.factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(this.factoryMock);

		this.dispatcher.dispatch(this.messageContext);

		assertThat(this.messageContext.getResponse()).isNotNull();

		verify(this.factoryMock);
	}

	@Test
	void testProcessUnsupportedEndpointException() {

		EndpointExceptionResolver resolverMock = createMock(EndpointExceptionResolver.class);
		this.dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolverMock));

		Object endpoint = new Object();
		Exception ex = new Exception();

		expect(resolverMock.resolveException(this.messageContext, endpoint, ex)).andReturn(false);

		replay(this.factoryMock, resolverMock);

		try {
			this.dispatcher.processEndpointException(this.messageContext, endpoint, ex);
		}
		catch (Exception result) {
			assertThat(result).isEqualTo(ex);
		}

		verify(this.factoryMock, resolverMock);
	}

	@Test
	void testNormalFlow() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		expect(interceptorMock1.handleRequest(this.messageContext, endpoint)).andReturn(true);
		expect(interceptorMock2.handleRequest(this.messageContext, endpoint)).andReturn(true);

		adapterMock.invoke(this.messageContext, endpoint);

		expect(interceptorMock2.handleResponse(this.messageContext, endpoint)).andReturn(true);
		expect(interceptorMock1.handleResponse(this.messageContext, endpoint)).andReturn(true);

		interceptorMock2.afterCompletion(this.messageContext, endpoint, null);
		interceptorMock1.afterCompletion(this.messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });

		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);

		// response required for interceptor invocation
		this.messageContext.getResponse();
		this.dispatcher.dispatch(this.messageContext);

		verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);
	}

	@Test
	void testFlowNoResponse() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });
		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);

		expect(interceptorMock1.handleRequest(this.messageContext, endpoint)).andReturn(true);
		expect(interceptorMock2.handleRequest(this.messageContext, endpoint)).andReturn(true);
		interceptorMock2.afterCompletion(this.messageContext, endpoint, null);
		interceptorMock1.afterCompletion(this.messageContext, endpoint, null);

		adapterMock.invoke(this.messageContext, endpoint);

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);

		this.dispatcher.dispatch(this.messageContext);

		verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);
	}

	@Test
	void testInterceptedRequestFlow() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		Object endpoint = new Object();

		expect(interceptorMock1.handleRequest(this.messageContext, endpoint)).andReturn(false);
		expect(interceptorMock1.handleResponse(this.messageContext, endpoint)).andReturn(true);
		interceptorMock1.afterCompletion(this.messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });

		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);

		// response required for interceptor invocation
		this.messageContext.getResponse();

		this.dispatcher.dispatch(this.messageContext);

		verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);
	}

	@Test
	void testInterceptedResponseFlow() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInterceptor interceptorMock1 = createStrictMock("interceptor1", EndpointInterceptor.class);
		EndpointInterceptor interceptorMock2 = createStrictMock("interceptor2", EndpointInterceptor.class);

		Object endpoint = new Object();
		expect(interceptorMock1.handleRequest(this.messageContext, endpoint)).andReturn(true);
		expect(interceptorMock2.handleRequest(this.messageContext, endpoint)).andReturn(false);
		expect(interceptorMock2.handleResponse(this.messageContext, endpoint)).andReturn(false);
		interceptorMock1.afterCompletion(this.messageContext, endpoint, null);
		interceptorMock2.afterCompletion(this.messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock1, interceptorMock2 });

		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);

		// response required for interceptor invocation
		this.messageContext.getResponse();

		this.dispatcher.dispatch(this.messageContext);

		verify(mappingMock, interceptorMock1, interceptorMock2, adapterMock, this.factoryMock);
	}

	@Test
	void testResolveExceptionsWithInterceptors() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointExceptionResolver resolverMock = createMock(EndpointExceptionResolver.class);
		this.dispatcher.setEndpointExceptionResolvers(Collections.singletonList(resolverMock));

		EndpointInterceptor interceptorMock = createStrictMock("interceptor1", EndpointInterceptor.class);

		expect(interceptorMock.handleRequest(this.messageContext, endpoint)).andReturn(true);

		adapterMock.invoke(this.messageContext, endpoint);
		RuntimeException exception = new RuntimeException();
		expectLastCall().andThrow(exception);

		expect(resolverMock.resolveException(this.messageContext, endpoint, exception)).andReturn(true);

		expect(interceptorMock.handleResponse(this.messageContext, endpoint)).andReturn(true);

		interceptorMock.afterCompletion(this.messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock });

		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(new MockWebServiceMessage());

		replay(mappingMock, interceptorMock, adapterMock, this.factoryMock, resolverMock);

		// response required for interceptor invocation
		this.messageContext.getResponse();
		try {
			this.dispatcher.dispatch(this.messageContext);
		}
		catch (RuntimeException ex) {

		}

		verify(mappingMock, interceptorMock, adapterMock, this.factoryMock, resolverMock);
	}

	@Test
	void testFaultFlow() throws Exception {

		EndpointAdapter adapterMock = createMock(EndpointAdapter.class);
		this.dispatcher.setEndpointAdapters(Collections.singletonList(adapterMock));

		Object endpoint = new Object();
		expect(adapterMock.supports(endpoint)).andReturn(true);

		EndpointMapping mappingMock = createMock(EndpointMapping.class);
		this.dispatcher.setEndpointMappings(Collections.singletonList(mappingMock));

		EndpointInterceptor interceptorMock = createStrictMock(EndpointInterceptor.class);

		expect(interceptorMock.handleRequest(this.messageContext, endpoint)).andReturn(true);
		adapterMock.invoke(this.messageContext, endpoint);
		expect(interceptorMock.handleFault(this.messageContext, endpoint)).andReturn(true);
		interceptorMock.afterCompletion(this.messageContext, endpoint, null);

		EndpointInvocationChain chain = new EndpointInvocationChain(endpoint,
				new EndpointInterceptor[] { interceptorMock });

		expect(mappingMock.getEndpoint(this.messageContext)).andReturn(chain);
		MockWebServiceMessage response = new MockWebServiceMessage();
		response.setFault(true);
		expect(this.factoryMock.createWebServiceMessage()).andReturn(response);

		replay(mappingMock, interceptorMock, adapterMock, this.factoryMock);

		// response required for interceptor invocation
		this.messageContext.getResponse();
		this.dispatcher.dispatch(this.messageContext);

		verify(mappingMock, interceptorMock, adapterMock, this.factoryMock);
	}

	@Test
	void testNoEndpointFound() {

		this.dispatcher.setEndpointMappings(Collections.emptyList());

		assertThatExceptionOfType(NoEndpointFoundException.class)
			.isThrownBy(() -> this.dispatcher.receive(this.messageContext));
	}

	@Test
	void testDetectStrategies() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();

		applicationContext.registerSingleton("mapping", PayloadRootQNameEndpointMapping.class);
		applicationContext.registerSingleton("adapter", PayloadEndpointAdapter.class);
		applicationContext.registerSingleton("resolver", SimpleSoapExceptionResolver.class);

		this.dispatcher.setApplicationContext(applicationContext);

		assertThat(this.dispatcher.getEndpointMappings()).hasSize(1);
		assertThat(this.dispatcher.getEndpointMappings()).hasOnlyElementsOfType(PayloadRootQNameEndpointMapping.class);

		assertThat(this.dispatcher.getEndpointAdapters()).hasSize(1);
		assertThat(this.dispatcher.getEndpointAdapters()).hasOnlyElementsOfTypes(PayloadEndpointAdapter.class);

		assertThat(this.dispatcher.getEndpointExceptionResolvers()).hasSize(1);
		assertThat(this.dispatcher.getEndpointExceptionResolvers())
			.hasOnlyElementsOfType(SimpleSoapExceptionResolver.class);
	}

	@Test
	void testDefaultStrategies() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		this.dispatcher.setApplicationContext(applicationContext);
	}

}
