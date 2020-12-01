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

package org.springframework.ws.server.endpoint.adapter.method;

import static org.assertj.core.api.Assertions.*;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.StringResult;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractPayloadMethodProcessorTestCase extends AbstractMethodArgumentResolverTestCase {

	private AbstractPayloadSourceMethodProcessor processor;

	private MethodParameter[] supportedParameters;

	private MethodParameter[] supportedReturnTypes;

	@BeforeEach
	public final void setUp() throws NoSuchMethodException {

		processor = createProcessor();
		supportedParameters = createSupportedParameters();
		supportedReturnTypes = createSupportedReturnTypes();
	}

	protected abstract AbstractPayloadSourceMethodProcessor createProcessor();

	protected abstract MethodParameter[] createSupportedParameters() throws NoSuchMethodException;

	protected abstract MethodParameter[] createSupportedReturnTypes() throws NoSuchMethodException;

	@Test
	public void supportsParameter() throws NoSuchMethodException {

		for (MethodParameter supportedParameter : supportedParameters) {
			assertThat(processor.supportsParameter(supportedParameter)).isTrue();
		}

		MethodParameter unsupportedParameter = new MethodParameter(getClass().getMethod("unsupported", String.class), 0);

		assertThat(processor.supportsParameter(unsupportedParameter)).isFalse();
	}

	@Test
	public void supportsReturnType() throws NoSuchMethodException {

		for (MethodParameter supportedReturnType : supportedReturnTypes) {
			assertThat(processor.supportsReturnType(supportedReturnType)).isTrue();
		}

		MethodParameter unsupportedReturnType = new MethodParameter(getClass().getMethod("unsupported", String.class), -1);

		assertThat(processor.supportsReturnType(unsupportedReturnType)).isFalse();
	}

	@Test
	public void saajArgument() throws Exception {
		testResolveArgument(createSaajMessageContext());
	}

	@Test
	public void mockArgument() throws Exception {
		testResolveArgument(createMockMessageContext());
	}

	@Test
	public void axiomCachingArgument() throws Exception {
		testResolveArgument(createCachingAxiomMessageContext());
	}

	@Test
	public void axiomNonCachingArgument() throws Exception {
		testResolveArgument(createNonCachingAxiomMessageContext());
	}

	private void testResolveArgument(MessageContext messageContext) throws Exception {

		for (MethodParameter supportedParameter : supportedParameters) {

			Object argument = processor.resolveArgument(messageContext, supportedParameter);

			assertThat(supportedParameter.getParameterType().isInstance(argument)).isTrue();
			testArgument(argument, supportedParameter);
		}
	}

	protected void testArgument(Object argument, MethodParameter parameter) {}

	@Test
	public void saajReturnValue() throws Exception {
		testHandleReturnValue(createSaajMessageContext());
	}

	@Test
	public void mockReturnValue() throws Exception {
		testHandleReturnValue(createMockMessageContext());
	}

	@Test
	public void axiomCachingReturnValue() throws Exception {
		testHandleReturnValue(createCachingAxiomMessageContext());
	}

	@Test
	public void axiomNonCachingReturnValue() throws Exception {
		testHandleReturnValue(createNonCachingAxiomMessageContext());
	}

	private void testHandleReturnValue(MessageContext messageContext) throws Exception {

		for (MethodParameter supportedReturnType : supportedReturnTypes) {

			Object returnValue = getReturnValue(supportedReturnType);
			processor.handleReturnValue(messageContext, supportedReturnType, returnValue);

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
