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

package org.springframework.ws.server.endpoint.adapter.method.jaxb;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;

/**
 * Implementation of {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports {@link JAXBElement} objects.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class JaxbElementPayloadMethodProcessor extends AbstractJaxb2PayloadMethodProcessor {

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		Class<?> parameterType = parameter.getParameterType();
		Type genericType = parameter.getGenericParameterType();
		return JAXBElement.class.equals(parameterType) && genericType instanceof ParameterizedType;
	}

	@Override
	public JAXBElement<?> resolveArgument(MessageContext messageContext, MethodParameter parameter)
			throws JAXBException {
		ParameterizedType parameterizedType = (ParameterizedType) parameter.getGenericParameterType();
		Class<?> clazz = (Class) parameterizedType.getActualTypeArguments()[0];
		return unmarshalElementFromRequestPayload(messageContext, clazz);
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		Class<?> parameterType = returnType.getParameterType();
		return JAXBElement.class.isAssignableFrom(parameterType);
	}

	@Override
	protected void handleReturnValueInternal(MessageContext messageContext, MethodParameter returnType, Object returnValue)
			throws JAXBException {
		JAXBElement<?> element = (JAXBElement<?>) returnValue;
		marshalToResponsePayload(messageContext, element.getDeclaredType(), element);
	}
}
