/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method.jaxb;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;

/**
 * Implementation of
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver
 * MethodArgumentResolver} and
 * {@link org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler
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
	public @Nullable JAXBElement<?> resolveArgument(MessageContext messageContext, MethodParameter parameter)
			throws JAXBException {
		ParameterizedType parameterizedType = (ParameterizedType) parameter.getGenericParameterType();
		Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
		return unmarshalElementFromRequestPayload(messageContext, clazz);
	}

	@Override
	protected boolean supportsResponsePayloadReturnType(MethodParameter returnType) {
		Class<?> parameterType = returnType.getParameterType();
		return JAXBElement.class.isAssignableFrom(parameterType);
	}

	@Override
	protected void handleReturnValueInternal(MessageContext messageContext, MethodParameter returnType,
			Object returnValue) throws JAXBException {
		JAXBElement<?> element = (JAXBElement<?>) returnValue;
		marshalToResponsePayload(messageContext, element.getDeclaredType(), element);
	}

}
