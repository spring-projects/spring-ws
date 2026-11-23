/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;

/**
 * Implementation of {@link MethodArgumentResolver} that supports {@link MessageContext}
 * arguments.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class MessageContextMethodArgumentResolver implements MethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return MessageContext.class.equals(parameter.getParameterType());
	}

	@Override
	public MessageContext resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception {
		return messageContext;
	}

}
