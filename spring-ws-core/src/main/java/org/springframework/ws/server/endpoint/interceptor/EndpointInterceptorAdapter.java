/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * Default implementation of the {@code EndpointInterceptor} interface, for simplified
 * implementation of pre-only/post-only interceptors.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class EndpointInterceptorAdapter implements EndpointInterceptor {

	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Returns {@code false}.
	 */
	public boolean understands(Element header) {
		return false;
	}

	/**
	 * Returns {@code true}.
	 * @return {@code true}
	 */
	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * Returns {@code true}.
	 * @return {@code true}
	 */
	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * Returns {@code true}.
	 * @return {@code true}
	 */
	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * Does nothing by default.
	 */
	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, @Nullable Exception ex)
			throws Exception {
	}

}
