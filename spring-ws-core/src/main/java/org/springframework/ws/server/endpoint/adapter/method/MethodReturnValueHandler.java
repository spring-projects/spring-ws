/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter.method;

import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.ws.context.MessageContext;

/**
 * Strategy interface used to handle method return values. This interface is used to allow
 * the {@link org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter
 * DefaultMethodEndpointAdapter} to be indefinitely extensible.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface MethodReturnValueHandler {

	/**
	 * Indicates whether the given {@linkplain MethodParameter method return type} is
	 * supported by this handler.
	 * @param returnType the method return type to check
	 * @return {@code true} if this handler supports the supplied return type;
	 * {@code false} otherwise
	 */
	boolean supportsReturnType(MethodParameter returnType);

	/**
	 * Handles the given return value.
	 * @param messageContext the current message context
	 * @param returnType the return type to handle. This type must have previously been
	 * passed to the {@link #supportsReturnType(MethodParameter)} method of this
	 * interface, which must have returned {@code true}.
	 * @param returnValue the return value to handle
	 * @throws Exception in case of errors
	 */
	void handleReturnValue(MessageContext messageContext, MethodParameter returnType, @Nullable Object returnValue)
			throws Exception;

}
