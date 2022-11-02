/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.adapter.method;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

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

public class MarshallingPayloadMethodProcessorTest extends AbstractMethodArgumentResolverTestCase {

	private MarshallingPayloadMethodProcessor processor;

	private GenericMarshaller marshaller;

	private GenericUnmarshaller unmarshaller;

	private MethodParameter supportedParameter;

	private MethodParameter supportedReturnType;

	@BeforeEach
	public void setUp() throws Exception {

		marshaller = createMock("marshaller", GenericMarshaller.class);
		unmarshaller = createMock("unmarshaller", GenericUnmarshaller.class);
		processor = new MarshallingPayloadMethodProcessor(marshaller, unmarshaller);
		supportedParameter = new MethodParameter(getClass().getMethod("method", MyObject.class), 0);
		supportedReturnType = new MethodParameter(getClass().getMethod("method", MyObject.class), -1);
	}

	@Test
	public void supportsParameterSupported() {

		expect(unmarshaller.supports(isA(Type.class))).andReturn(true);

		replay(marshaller, unmarshaller);

		assertThat(processor.supportsParameter(supportedParameter)).isTrue();

		verify(marshaller, unmarshaller);
	}

	@Test
	public void supportsParameterUnsupported() {

		expect(unmarshaller.supports(isA(Type.class))).andReturn(false);

		replay(marshaller, unmarshaller);

		assertThat(processor.supportsParameter(supportedParameter)).isFalse();

		verify(marshaller, unmarshaller);
	}

	@Test
	public void supportsParameterNoUnmarshallerSupported() {

		processor = new MarshallingPayloadMethodProcessor();
		processor.setMarshaller(marshaller);

		replay(marshaller, unmarshaller);

		assertThat(processor.supportsParameter(supportedParameter)).isFalse();

		verify(marshaller, unmarshaller);
	}

	@Test
	public void supportsReturnTypeSupported() {

		expect(marshaller.supports(isA(Type.class))).andReturn(true);

		replay(marshaller, unmarshaller);

		assertThat(processor.supportsReturnType(supportedReturnType)).isTrue();

		verify(marshaller, unmarshaller);
	}

	@Test
	public void supportsReturnTypeUnsupported() {

		expect(marshaller.supports(isA(Type.class))).andReturn(false);

		replay(marshaller, unmarshaller);

		assertThat(processor.supportsReturnType(supportedReturnType)).isFalse();

		verify(marshaller, unmarshaller);
	}

	@Test
	public void supportsReturnTypeNoMarshaller() {

		processor = new MarshallingPayloadMethodProcessor();
		processor.setUnmarshaller(unmarshaller);

		replay(marshaller, unmarshaller);

		assertThat(processor.supportsReturnType(supportedReturnType)).isFalse();

		verify(marshaller, unmarshaller);
	}

	@Test
	public void resolveArgument() throws Exception {

		MyObject expected = new MyObject();

		expect(unmarshaller.unmarshal(isA(Source.class))).andReturn(expected);

		replay(marshaller, unmarshaller);
		MessageContext messageContext = createMockMessageContext();

		Object result = processor.resolveArgument(messageContext, supportedParameter);

		assertThat(result).isEqualTo(expected);

		verify(marshaller, unmarshaller);
	}

	@Test
	public void resolveArgumentNoUnmarshaller() {

		assertThatIllegalStateException().isThrownBy(() -> {

			processor = new MarshallingPayloadMethodProcessor();
			processor.setMarshaller(marshaller);

			replay(marshaller, unmarshaller);

			MessageContext messageContext = createMockMessageContext();

			processor.resolveArgument(messageContext, supportedParameter);
		});
	}

	@Test
	public void handleReturnValue() throws Exception {

		MyObject returnValue = new MyObject();

		marshaller.marshal(eq(returnValue), isA(Result.class));

		replay(marshaller, unmarshaller);
		MessageContext messageContext = createMockMessageContext();

		processor.handleReturnValue(messageContext, supportedReturnType, returnValue);

		verify(marshaller, unmarshaller);
	}

	@Test
	public void handleReturnValueNoMarshaller() {

		assertThatIllegalStateException().isThrownBy(() -> {

			processor = new MarshallingPayloadMethodProcessor();
			processor.setUnmarshaller(unmarshaller);

			MyObject returnValue = new MyObject();

			replay(marshaller, unmarshaller);
			MessageContext messageContext = createMockMessageContext();

			processor.handleReturnValue(messageContext, supportedReturnType, returnValue);
		});

	}

	@ResponsePayload
	public MyObject method(@RequestPayload MyObject object) {
		return object;
	}

	public static class MyObject {

	}
}
