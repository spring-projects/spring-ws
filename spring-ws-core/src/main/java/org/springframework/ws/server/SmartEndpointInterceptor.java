/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server;

import org.springframework.ws.context.MessageContext;

/**
 * Extension of the {@link EndpointInterceptor} interface that adds a way to decide
 * whether the interceptor should intercept a given message context.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface SmartEndpointInterceptor extends EndpointInterceptor {

	/**
	 * Indicates whether this interceptor should intercept the given message context.
	 * @param messageContext contains the incoming request message
	 * @param endpoint chosen endpoint to invoke
	 * @return {@code true} to indicate that this interceptor applies; {@code false}
	 * otherwise
	 */
	boolean shouldIntercept(MessageContext messageContext, Object endpoint);

}
