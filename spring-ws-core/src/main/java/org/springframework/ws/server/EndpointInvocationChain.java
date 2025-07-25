/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
