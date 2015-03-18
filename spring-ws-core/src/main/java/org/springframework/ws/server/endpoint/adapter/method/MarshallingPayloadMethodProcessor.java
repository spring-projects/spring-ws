/*
 * Copyright 2005-2014 the original author or authors.
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

import org.springframework.core.MethodParameter;
import org.springframework.oxm.GenericMarshaller;
import org.springframework.oxm.GenericUnmarshaller;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.support.MarshallingUtils;

/**
 * Implementation of {@link MethodArgumentResolver} and {@link MethodReturnValueHandler} that uses {@link Marshaller}
 * and {@link Unmarshaller} to support marshalled objects.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class MarshallingPayloadMethodProcessor extends AbstractPayloadMethodProcessor {

	private Marshaller marshaller;

	private Unmarshaller unmarshaller;

	/**
	 * Creates a new {@code MarshallingPayloadMethodProcessor}. The {@link Marshaller} and {@link Unmarshaller} must be
	 * injected using properties.
	 *
	 * @see #setMarshaller(Marshaller)
	 * @see #setUnmarshaller(Unmarshaller)
	 */
	public MarshallingPayloadMethodProcessor() {
	}

	/**
	 * Creates a new {@code MarshallingPayloadMethodProcessor} with the given marshaller. If the given {@link
	 * Marshaller} also implements the {@link Unmarshaller} interface, it is used for both marshalling and
	 * unmarshalling. Otherwise, an exception is thrown.
	 *
	 * <p>Note that all {@link Marshaller} implementations in Spring also implement the {@link Unmarshaller} interface, so
	 * that you can safely use this constructor.
	 *
	 * @param marshaller object used as marshaller and unmarshaller
	 * @throws IllegalArgumentException when {@code marshaller} does not implement the {@link Unmarshaller} interface
	 */
	public MarshallingPayloadMethodProcessor(Marshaller marshaller) {
		Assert.notNull(marshaller, "marshaller must not be null");
		Assert.isInstanceOf(Unmarshaller.class, marshaller);
		setMarshaller(marshaller);
		setUnmarshaller((Unmarshaller) marshaller);
	}

	/**
	 * Creates a new {@code MarshallingPayloadMethodProcessor} with the given marshaller and unmarshaller.
	 *
	 * @param marshaller   the marshaller to use
	 * @param unmarshaller the unmarshaller to use
	 */
	public MarshallingPayloadMethodProcessor(Marshaller marshaller, Unmarshaller unmarshaller) {
		Assert.notNull(marshaller, "marshaller must not be null");
		Assert.notNull(unmarshaller, "unmarshaller must not be null");
		setMarshaller(marshaller);
		setUnmarshaller(unmarshaller);
	}

	/**
	 * Returns the marshaller used for transforming objects into XML.
	 */
	public Marshaller getMarshaller() {
		return marshaller;
	}

	/**
	 * Sets the marshaller used for transforming objects into XML.
	 */
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	/**
	 * Returns the unmarshaller used for transforming XML into objects.
	 */
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	/**
	 * Sets the unmarshaller used for transforming XML into objects.
	 */
	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		Unmarshaller unmarshaller = getUnmarshaller();
		if (unmarshaller == null) {
			return false;
		}
		else if (unmarshaller instanceof GenericUnmarshaller) {
			return ((GenericUnmarshaller) unmarshaller).supports(parameter.getGenericParameterType());
		}
		else {
			return unmarshaller.supports(parameter.getParameterType());
		}
	}

	@Override
	public Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
		Unmarshaller unmarshaller = getUnmarshaller();
		Assert.state(unmarshaller != null, "unmarshaller must not be null");

		WebServiceMessage request = messageContext.getRequest();
		Object argument = MarshallingUtils.unmarshal(unmarshaller, request);
		if (logger.isDebugEnabled()) {
			logger.debug("Unmarshalled payload request to [" + argument + "]");
		}
		return argument;
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		Marshaller marshaller = getMarshaller();
		if (marshaller == null) {
			return false;
		}
		else if (marshaller instanceof GenericMarshaller) {
			GenericMarshaller genericMarshaller = (GenericMarshaller) marshaller;
			return genericMarshaller.supports(returnType.getGenericParameterType());
		}
		else {
			return marshaller.supports(returnType.getParameterType());
		}
	}

	@Override
	public void handleReturnValue(MessageContext messageContext, MethodParameter returnType, Object returnValue)
			throws Exception {
		if (returnValue == null) {
			return;
		}
		Marshaller marshaller = getMarshaller();
		Assert.state(marshaller != null, "marshaller must not be null");

		if (logger.isDebugEnabled()) {
			logger.debug("Marshalling [" + returnValue + "] to response payload");
		}
		WebServiceMessage response = messageContext.getResponse();
		MarshallingUtils.marshal(marshaller, returnValue, response);
	}

}
