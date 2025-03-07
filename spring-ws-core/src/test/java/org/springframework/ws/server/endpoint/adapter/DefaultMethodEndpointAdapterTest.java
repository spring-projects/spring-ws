/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author Arjen Poutsma
 */
public class DefaultMethodEndpointAdapterTest {

	private DefaultMethodEndpointAdapter adapter;

	private MethodArgumentResolver argumentResolver1;

	private MethodArgumentResolver argumentResolver2;

	private MethodReturnValueHandler returnValueHandler;

	private MethodEndpoint supportedEndpoint;

	private MethodEndpoint nullReturnValue;

	private MethodEndpoint unsupportedEndpoint;

	private MethodEndpoint exceptionEndpoint;

	private String supportedArgument;

	@BeforeEach
	public void setUp() throws Exception {

		this.adapter = new DefaultMethodEndpointAdapter();
		this.argumentResolver1 = createMock("stringResolver", MethodArgumentResolver.class);
		this.argumentResolver2 = createMock("intResolver", MethodArgumentResolver.class);
		this.returnValueHandler = createMock(MethodReturnValueHandler.class);
		this.adapter.setMethodArgumentResolvers(Arrays.asList(this.argumentResolver1, this.argumentResolver2));
		this.adapter.setMethodReturnValueHandlers(Collections.singletonList(this.returnValueHandler));
		this.supportedEndpoint = new MethodEndpoint(this, "supported", String.class, Integer.class);
		this.nullReturnValue = new MethodEndpoint(this, "nullReturnValue", String.class);
		this.unsupportedEndpoint = new MethodEndpoint(this, "unsupported", String.class);
		this.exceptionEndpoint = new MethodEndpoint(this, "exception", String.class);
	}

	@Test
	public void initDefaultStrategies() throws Exception {

		this.adapter = new DefaultMethodEndpointAdapter();
		this.adapter.setBeanClassLoader(DefaultMethodEndpointAdapterTest.class.getClassLoader());
		this.adapter.afterPropertiesSet();

		assertThat(this.adapter.getMethodArgumentResolvers()).isNotEmpty();
		assertThat(this.adapter.getMethodReturnValueHandlers()).isNotEmpty();
	}

	@Test
	public void supportsSupported() {

		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
		expect(this.argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);

		replay(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);

		boolean result = this.adapter.supports(this.supportedEndpoint);

		assertThat(result).isTrue();

		verify(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);
	}

	@Test
	public void supportsUnsupportedParameter() {

		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
		expect(this.argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(false);

		replay(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);

		boolean result = this.adapter.supports(this.unsupportedEndpoint);
		assertThat(result).isFalse();

		verify(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);
	}

	@Test
	public void supportsUnsupportedReturnType() {

		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(false);

		replay(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);

		boolean result = this.adapter.supports(this.unsupportedEndpoint);

		assertThat(result).isFalse();

		verify(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);
	}

	@Test
	public void invokeSupported() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String value = "Foo";

		// arg 0
		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

		// arg 1
		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
		expect(this.argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.argumentResolver2.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(42);

		expect(this.returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);
		this.returnValueHandler.handleReturnValue(eq(messageContext), isA(MethodParameter.class), eq(value));

		replay(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);

		this.adapter.invoke(messageContext, this.supportedEndpoint);

		assertThat(this.supportedArgument).isEqualTo(value);

		verify(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);
	}

	@Test
	public void invokeNullReturnValue() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String value = "Foo";

		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

		expect(this.returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);
		this.returnValueHandler.handleReturnValue(eq(messageContext), isA(MethodParameter.class), isNull());

		replay(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);

		this.adapter.invoke(messageContext, this.nullReturnValue);

		assertThat(this.supportedArgument).isEqualTo(value);

		verify(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);
	}

	@Test
	public void invokeException() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String value = "Foo";

		expect(this.argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(this.argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

		replay(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);

		try {
			this.adapter.invoke(messageContext, this.exceptionEndpoint);
			fail("IOException expected");
		}
		catch (IOException expected) {
			// expected
		}

		assertThat(this.supportedArgument).isEqualTo(value);

		verify(this.argumentResolver1, this.argumentResolver2, this.returnValueHandler);
	}

	public String supported(String s, Integer i) {

		this.supportedArgument = s;
		return s;

	}

	public String nullReturnValue(String s) {

		this.supportedArgument = s;
		return null;
	}

	public String unsupported(String s) {
		return s;
	}

	public String exception(String s) throws IOException {

		this.supportedArgument = s;
		throw new IOException(s);
	}

}
