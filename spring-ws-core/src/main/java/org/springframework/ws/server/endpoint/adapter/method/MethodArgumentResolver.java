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
 * Strategy interface used to resolve method parameters into arguments. This interface is
 * used to allow the
 * {@link org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter
 * DefaultMethodEndpointAdapter} to be indefinitely extensible.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface MethodArgumentResolver {

	/**
	 * Indicates whether the given {@linkplain MethodParameter method parameter} is
	 * supported by this resolver.
	 * @param parameter the method parameter to check
	 * @return {@code true} if this resolver supports the supplied parameter;
	 * {@code false} otherwise
	 */
	boolean supportsParameter(MethodParameter parameter);

	/**
	 * Resolves the given parameter into a method argument.
	 * @param messageContext the current message context
	 * @param parameter the parameter to resolve to an argument. This parameter must have
	 * previously been passed to the {@link #supportsParameter(MethodParameter)} method of
	 * this interface, which must have returned {@code true}.
	 * @return the resolved argument. May be {@code null}.
	 * @throws Exception in case of errors
	 */
	@Nullable Object resolveArgument(MessageContext messageContext, MethodParameter parameter) throws Exception;

}
