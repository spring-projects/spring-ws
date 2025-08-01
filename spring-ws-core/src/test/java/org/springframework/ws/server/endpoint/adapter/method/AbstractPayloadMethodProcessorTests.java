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

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.StringResult;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractPayloadMethodProcessorTests extends AbstractMethodArgumentResolverTests {

	private AbstractPayloadSourceMethodProcessor processor;

	private MethodParameter[] supportedParameters;

	private MethodParameter[] supportedReturnTypes;

	@BeforeEach
	public final void setUp() throws NoSuchMethodException {

		this.processor = createProcessor();
		this.supportedParameters = createSupportedParameters();
		this.supportedReturnTypes = createSupportedReturnTypes();
	}

	protected abstract AbstractPayloadSourceMethodProcessor createProcessor();

	protected abstract MethodParameter[] createSupportedParameters() throws NoSuchMethodException;

	protected abstract MethodParameter[] createSupportedReturnTypes() throws NoSuchMethodException;

	@Test
	void supportsParameter() throws NoSuchMethodException {

		for (MethodParameter supportedParameter : this.supportedParameters) {
			assertThat(this.processor.supportsParameter(supportedParameter)).isTrue();
		}

		MethodParameter unsupportedParameter = new MethodParameter(getClass().getMethod("unsupported", String.class),
				0);

		assertThat(this.processor.supportsParameter(unsupportedParameter)).isFalse();
	}

	@Test
	void supportsReturnType() throws NoSuchMethodException {

		for (MethodParameter supportedReturnType : this.supportedReturnTypes) {
			assertThat(this.processor.supportsReturnType(supportedReturnType)).isTrue();
		}

		MethodParameter unsupportedReturnType = new MethodParameter(getClass().getMethod("unsupported", String.class),
				-1);

		assertThat(this.processor.supportsReturnType(unsupportedReturnType)).isFalse();
	}

	@Test
	void saajArgument() throws Exception {
		testResolveArgument(createSaajMessageContext());
	}

	@Test
	void mockArgument() throws Exception {
		testResolveArgument(createMockMessageContext());
	}

	@Test
	void axiomCachingArgument() throws Exception {
		testResolveArgument(createCachingAxiomMessageContext());
	}

	@Test
	void axiomNonCachingArgument() throws Exception {
		testResolveArgument(createNonCachingAxiomMessageContext());
	}

	private void testResolveArgument(MessageContext messageContext) throws Exception {

		for (MethodParameter supportedParameter : this.supportedParameters) {

			Object argument = this.processor.resolveArgument(messageContext, supportedParameter);

			assertThat(supportedParameter.getParameterType().isInstance(argument)).isTrue();
			testArgument(argument, supportedParameter);
		}
	}

	protected void testArgument(Object argument, MethodParameter parameter) {
	}

	@Test
	void saajReturnValue() throws Exception {
		testHandleReturnValue(createSaajMessageContext());
	}

	@Test
	void mockReturnValue() throws Exception {
		testHandleReturnValue(createMockMessageContext());
	}

	@Test
	void axiomCachingReturnValue() throws Exception {
		testHandleReturnValue(createCachingAxiomMessageContext());
	}

	@Test
	void axiomNonCachingReturnValue() throws Exception {
		testHandleReturnValue(createNonCachingAxiomMessageContext());
	}

	private void testHandleReturnValue(MessageContext messageContext) throws Exception {

		for (MethodParameter supportedReturnType : this.supportedReturnTypes) {

			Object returnValue = getReturnValue(supportedReturnType);
			this.processor.handleReturnValue(messageContext, supportedReturnType, returnValue);

			assertThat(messageContext.hasResponse()).isTrue();

			Source responsePayload = messageContext.getResponse().getPayloadSource();
			StringResult result = new StringResult();
			transform(responsePayload, result);

			XmlAssert.assertThat(result.toString()).and(XML).ignoreWhitespace().areIdentical();
		}
	}

	protected abstract Object getReturnValue(MethodParameter returnType) throws Exception;

	public String unsupported(String s) {
		return s;
	}

}
