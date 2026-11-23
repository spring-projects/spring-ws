/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.context.MessageContext;

/**
 * Defines the interface for objects than can resolve exceptions thrown during endpoint
 * execution.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface EndpointExceptionResolver {

	/**
	 * Try to resolve the given exception that got thrown during on endpoint execution.
	 * @param messageContext current message context
	 * @param endpoint the executed endpoint, or null if none chosen at the time of the
	 * exception
	 * @param ex the exception that got thrown during endpoint execution
	 * @return {@code true} if resolved; {@code false} otherwise
	 */
	boolean resolveException(MessageContext messageContext, @Nullable Object endpoint, Exception ex);

}
