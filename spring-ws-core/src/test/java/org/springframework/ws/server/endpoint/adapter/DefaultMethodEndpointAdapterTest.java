/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

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

/** @author Arjen Poutsma */
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

		adapter = new DefaultMethodEndpointAdapter();
		argumentResolver1 = createMock("stringResolver", MethodArgumentResolver.class);
		argumentResolver2 = createMock("intResolver", MethodArgumentResolver.class);
		returnValueHandler = createMock(MethodReturnValueHandler.class);
		adapter.setMethodArgumentResolvers(Arrays.asList(argumentResolver1, argumentResolver2));
		adapter.setMethodReturnValueHandlers(Collections.singletonList(returnValueHandler));
		supportedEndpoint = new MethodEndpoint(this, "supported", String.class, Integer.class);
		nullReturnValue = new MethodEndpoint(this, "nullReturnValue", String.class);
		unsupportedEndpoint = new MethodEndpoint(this, "unsupported", String.class);
		exceptionEndpoint = new MethodEndpoint(this, "exception", String.class);
	}

	@Test
	public void initDefaultStrategies() throws Exception {

		adapter = new DefaultMethodEndpointAdapter();
		adapter.setBeanClassLoader(DefaultMethodEndpointAdapterTest.class.getClassLoader());
		adapter.afterPropertiesSet();

		assertThat(adapter.getMethodArgumentResolvers()).isNotEmpty();
		assertThat(adapter.getMethodReturnValueHandlers()).isNotEmpty();
	}

	@Test
	public void supportsSupported() {

		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
		expect(argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);

		replay(argumentResolver1, argumentResolver2, returnValueHandler);

		boolean result = adapter.supports(supportedEndpoint);

		assertThat(result).isTrue();

		verify(argumentResolver1, argumentResolver2, returnValueHandler);
	}

	@Test
	public void supportsUnsupportedParameter() {

		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
		expect(argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(false);

		replay(argumentResolver1, argumentResolver2, returnValueHandler);

		boolean result = adapter.supports(unsupportedEndpoint);
		assertThat(result).isFalse();

		verify(argumentResolver1, argumentResolver2, returnValueHandler);
	}

	@Test
	public void supportsUnsupportedReturnType() {

		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(false);

		replay(argumentResolver1, argumentResolver2, returnValueHandler);

		boolean result = adapter.supports(unsupportedEndpoint);

		assertThat(result).isFalse();

		verify(argumentResolver1, argumentResolver2, returnValueHandler);
	}

	@Test
	public void invokeSupported() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String value = "Foo";

		// arg 0
		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

		// arg 1
		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(false);
		expect(argumentResolver2.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(argumentResolver2.resolveArgument(eq(messageContext), isA(MethodParameter.class)))
				.andReturn(new Integer(42));

		expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);
		returnValueHandler.handleReturnValue(eq(messageContext), isA(MethodParameter.class), eq(value));

		replay(argumentResolver1, argumentResolver2, returnValueHandler);

		adapter.invoke(messageContext, supportedEndpoint);

		assertThat(supportedArgument).isEqualTo(value);

		verify(argumentResolver1, argumentResolver2, returnValueHandler);
	}

	@Test
	public void invokeNullReturnValue() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String value = "Foo";

		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

		expect(returnValueHandler.supportsReturnType(isA(MethodParameter.class))).andReturn(true);
		returnValueHandler.handleReturnValue(eq(messageContext), isA(MethodParameter.class), isNull());

		replay(argumentResolver1, argumentResolver2, returnValueHandler);

		adapter.invoke(messageContext, nullReturnValue);

		assertThat(supportedArgument).isEqualTo(value);

		verify(argumentResolver1, argumentResolver2, returnValueHandler);
	}

	@Test
	public void invokeException() throws Exception {

		MockWebServiceMessage request = new MockWebServiceMessage("<root xmlns='http://springframework.org'/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());

		String value = "Foo";

		expect(argumentResolver1.supportsParameter(isA(MethodParameter.class))).andReturn(true);
		expect(argumentResolver1.resolveArgument(eq(messageContext), isA(MethodParameter.class))).andReturn(value);

		replay(argumentResolver1, argumentResolver2, returnValueHandler);

		try {
			adapter.invoke(messageContext, exceptionEndpoint);
			fail("IOException expected");
		} catch (IOException expected) {
			// expected
		}

		assertThat(supportedArgument).isEqualTo(value);

		verify(argumentResolver1, argumentResolver2, returnValueHandler);
	}

	public String supported(String s, Integer i) {

		supportedArgument = s;
		return s;

	}

	public String nullReturnValue(String s) {

		supportedArgument = s;
		return null;
	}

	public String unsupported(String s) {
		return s;
	}

	public String exception(String s) throws IOException {

		supportedArgument = s;
		throw new IOException(s);
	}
}
