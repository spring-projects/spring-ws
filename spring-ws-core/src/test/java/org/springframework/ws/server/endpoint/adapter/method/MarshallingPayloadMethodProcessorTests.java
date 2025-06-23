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

package org.springframework.ws.server.endpoint.adapter.method;

import java.lang.reflect.Type;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.oxm.GenericMarshaller;
import org.springframework.oxm.GenericUnmarshaller;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class MarshallingPayloadMethodProcessorTests extends AbstractMethodArgumentResolverTests {

	private MarshallingPayloadMethodProcessor processor;

	private GenericMarshaller marshaller;

	private GenericUnmarshaller unmarshaller;

	private MethodParameter supportedParameter;

	private MethodParameter supportedReturnType;

	@BeforeEach
	void setUp() throws Exception {

		this.marshaller = createMock("marshaller", GenericMarshaller.class);
		this.unmarshaller = createMock("unmarshaller", GenericUnmarshaller.class);
		this.processor = new MarshallingPayloadMethodProcessor(this.marshaller, this.unmarshaller);
		this.supportedParameter = new MethodParameter(getClass().getMethod("method", MyObject.class), 0);
		this.supportedReturnType = new MethodParameter(getClass().getMethod("method", MyObject.class), -1);
	}

	@Test
	void supportsParameterSupported() {

		expect(this.unmarshaller.supports(isA(Type.class))).andReturn(true);

		replay(this.marshaller, this.unmarshaller);

		assertThat(this.processor.supportsParameter(this.supportedParameter)).isTrue();

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void supportsParameterUnsupported() {

		expect(this.unmarshaller.supports(isA(Type.class))).andReturn(false);

		replay(this.marshaller, this.unmarshaller);

		assertThat(this.processor.supportsParameter(this.supportedParameter)).isFalse();

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void supportsParameterNoUnmarshallerSupported() {

		this.processor = new MarshallingPayloadMethodProcessor();
		this.processor.setMarshaller(this.marshaller);

		replay(this.marshaller, this.unmarshaller);

		assertThat(this.processor.supportsParameter(this.supportedParameter)).isFalse();

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void supportsReturnTypeSupported() {

		expect(this.marshaller.supports(isA(Type.class))).andReturn(true);

		replay(this.marshaller, this.unmarshaller);

		assertThat(this.processor.supportsReturnType(this.supportedReturnType)).isTrue();

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void supportsReturnTypeUnsupported() {

		expect(this.marshaller.supports(isA(Type.class))).andReturn(false);

		replay(this.marshaller, this.unmarshaller);

		assertThat(this.processor.supportsReturnType(this.supportedReturnType)).isFalse();

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void supportsReturnTypeNoMarshaller() {

		this.processor = new MarshallingPayloadMethodProcessor();
		this.processor.setUnmarshaller(this.unmarshaller);

		replay(this.marshaller, this.unmarshaller);

		assertThat(this.processor.supportsReturnType(this.supportedReturnType)).isFalse();

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void resolveArgument() throws Exception {

		MyObject expected = new MyObject();

		expect(this.unmarshaller.unmarshal(isA(Source.class))).andReturn(expected);

		replay(this.marshaller, this.unmarshaller);
		MessageContext messageContext = createMockMessageContext();

		Object result = this.processor.resolveArgument(messageContext, this.supportedParameter);

		assertThat(result).isEqualTo(expected);

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void resolveArgumentNoUnmarshaller() {

		assertThatIllegalStateException().isThrownBy(() -> {

			this.processor = new MarshallingPayloadMethodProcessor();
			this.processor.setMarshaller(this.marshaller);

			replay(this.marshaller, this.unmarshaller);

			MessageContext messageContext = createMockMessageContext();

			this.processor.resolveArgument(messageContext, this.supportedParameter);
		});
	}

	@Test
	void handleReturnValue() throws Exception {

		MyObject returnValue = new MyObject();

		this.marshaller.marshal(eq(returnValue), isA(Result.class));

		replay(this.marshaller, this.unmarshaller);
		MessageContext messageContext = createMockMessageContext();

		this.processor.handleReturnValue(messageContext, this.supportedReturnType, returnValue);

		verify(this.marshaller, this.unmarshaller);
	}

	@Test
	void handleReturnValueNoMarshaller() {

		assertThatIllegalStateException().isThrownBy(() -> {

			this.processor = new MarshallingPayloadMethodProcessor();
			this.processor.setUnmarshaller(this.unmarshaller);

			MyObject returnValue = new MyObject();

			replay(this.marshaller, this.unmarshaller);
			MessageContext messageContext = createMockMessageContext();

			this.processor.handleReturnValue(messageContext, this.supportedReturnType, returnValue);
		});

	}

	@ResponsePayload
	public MyObject method(@RequestPayload MyObject object) {
		return object;
	}

	public static class MyObject {

	}

}
