/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server;

import org.jspecify.annotations.Nullable;

/**
 * Endpoint invocation chain, consisting of an endpoint object and any preprocessing
 * interceptors.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see EndpointInterceptor
 */
public class EndpointInvocationChain {

	private final Object endpoint;

	private EndpointInterceptor @Nullable [] interceptors;

	/**
	 * Create new {@code EndpointInvocationChain}.
	 * @param endpoint the endpoint object to invoke
	 */
	public EndpointInvocationChain(Object endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Create new {@code EndpointInvocationChain}.
	 * @param endpoint the endpoint object to invoke
	 * @param interceptors the array of interceptors to apply
	 */
	public EndpointInvocationChain(Object endpoint, EndpointInterceptor @Nullable [] interceptors) {
		this.endpoint = endpoint;
		this.interceptors = interceptors;
	}

	/**
	 * Returns the endpoint object to invoke.
	 * @return the endpoint object
	 */
	public Object getEndpoint() {
		return this.endpoint;
	}

	/**
	 * Returns the array of interceptors to apply before the handler executes.
	 * @return the array of interceptors
	 */
	public EndpointInterceptor @Nullable [] getInterceptors() {
		return this.interceptors;
	}

}
