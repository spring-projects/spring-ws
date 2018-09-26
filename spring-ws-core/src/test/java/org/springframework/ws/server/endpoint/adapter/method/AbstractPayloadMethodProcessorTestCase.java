/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter.method;

import javax.xml.transform.Source;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;
import org.springframework.xml.transform.StringResult;

import org.junit.Before;
import org.junit.Test;

import static org.xmlunit.assertj.XmlAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractPayloadMethodProcessorTestCase extends AbstractMethodArgumentResolverTestCase {

	private AbstractPayloadSourceMethodProcessor processor;

	private MethodParameter[] supportedParameters;

	private MethodParameter[] supportedReturnTypes;

	@Before
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
			assertTrue("processor does not support " + supportedParameter.getParameterType() + " parameter",
					processor.supportsParameter(supportedParameter));
		}
		MethodParameter unsupportedParameter =
				new MethodParameter(getClass().getMethod("unsupported", String.class), 0);
		assertFalse("processor supports invalid parameter", processor.supportsParameter(unsupportedParameter));
	}

	@Test
	public void supportsReturnType() throws NoSuchMethodException {
		for (MethodParameter supportedReturnType : supportedReturnTypes) {
			assertTrue("processor does not support " + supportedReturnType.getParameterType() + " return type",
					processor.supportsReturnType(supportedReturnType));
		}
		MethodParameter unsupportedReturnType =
				new MethodParameter(getClass().getMethod("unsupported", String.class), -1);
		assertFalse("processor supports invalid return type", processor.supportsReturnType(unsupportedReturnType));
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

			assertTrue(argument + " is not an instance of " + supportedParameter.getParameterType(),
					supportedParameter.getParameterType().isInstance(argument));
			testArgument(argument, supportedParameter);
		}
	}


	protected void testArgument(Object argument, MethodParameter parameter) {
	}

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
			assertTrue("No response created", messageContext.hasResponse());
			Source responsePayload = messageContext.getResponse().getPayloadSource();
			StringResult result = new StringResult();
			transform(responsePayload, result);
			assertThat(result.toString()).and(XML).areSimilar();
		}
	}

	protected abstract Object getReturnValue(MethodParameter returnType) throws Exception;

	public String unsupported(String s) {
		return s;
	}

}