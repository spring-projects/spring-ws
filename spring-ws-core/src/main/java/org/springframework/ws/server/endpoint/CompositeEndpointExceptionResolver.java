/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;

/**
 * A composite of {@linkplain AbstractEndpointExceptionResolver endpoint exception
 * resolvers}.
 *
 * @author Stephane Nicoll
 * @since 4.1.0
 */
public class CompositeEndpointExceptionResolver implements EndpointExceptionResolver {

	private final Iterable<AbstractEndpointExceptionResolver> resolvers;

	public CompositeEndpointExceptionResolver(Iterable<AbstractEndpointExceptionResolver> resolvers) {
		this.resolvers = resolvers;
	}

	@Override
	public final boolean resolveException(MessageContext messageContext, @Nullable Object endpoint, Exception ex) {
		AbstractEndpointExceptionResolver currentResolver = null;
		for (AbstractEndpointExceptionResolver resolver : this.resolvers) {
			currentResolver = resolver;
			if (currentResolver.resolveException(messageContext, endpoint, ex)) {
				return true;
			}
		}
		if (currentResolver != null) {
			currentResolver.logException(messageContext, ex);
		}
		return false;
	}

}
