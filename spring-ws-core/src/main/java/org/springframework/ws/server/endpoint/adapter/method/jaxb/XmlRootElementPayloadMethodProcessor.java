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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;

/**
 * Implementation of {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
 * MethodReturnValueHandler} that supports parameters annotated with {@link XmlRootElement @XmlRootElement} or
 * {@link XmlType @XmlType}, and return values annotated with {@link XmlRootElement @XmlRootElement}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class XmlRootElementPayloadMethodProcessor extends AbstractJaxb2PayloadMethodProcessor {

	@Override
	protected boolean supportsRequestPayloadParameter(MethodParameter parameter) {
		Class<?> parameterType = parameter.getParameterType();
		return parameterType.isAnnotationPresent(XmlRootElement.class) || parameterType.isAnnotationPresent(XmlType.class);
	}

	@Override
	public Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws JAXBException {
		Class<?> parameterType = parameter.getParameterType();

		if (parameterType.isAnnotationPresent(XmlRootElement.class)) {
			return unmarshalFromRequestPayload(messageContext, parameterType);
		} else {
			JAXBElement<?> element = unmarshalElementFromRequestPayload(messageContext, parameterType);
			return element != null ? element.getValue() : null;
		}
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		Class<?> parameterType = returnType.getParameterType();
		return parameterType.isAnnotationPresent(XmlRootElement.class);
	}

	@Override
	protected void handleReturnValueInternal(MessageContext messageContext, MethodParameter returnType,
			Object returnValue) throws JAXBException {
		Class<?> parameterType = returnType.getParameterType();
		marshalToResponsePayload(messageContext, parameterType, returnValue);
	}

}
