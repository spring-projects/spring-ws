/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.transport.http;

import org.apache.hc.client5.http.classic.HttpClient;

import org.springframework.util.Assert;

/**
 * {@code AbstractHttpComponents5MessageSender} implementation that defines the underlying
 * <a href="http://hc.apache.org/httpcomponents-client">Apache HttpClient</a> that
 * executes POST requests.
 * <p>
 * The {@link HttpClient} can be provided as-is or configured via the convenient
 * {@link HttpComponents5ClientFactory}
 * <p>
 * For convenience method use to customize the underlying {@link HttpClient}, consider
 * using {@link HttpComponents5MessageSender} instead.
 *
 * @author Stephane Nicoll
 * @since 4.1.0
 * @see HttpClient
 * @see HttpComponents5ClientFactory
 */
public class SimpleHttpComponents5MessageSender extends AbstractHttpComponents5MessageSender {

	private final HttpClient httpClient;

	/**
	 * Creates an instance with the given {@link HttpClient}.
	 * @param httpClient the http client to use
	 */
	public SimpleHttpComponents5MessageSender(HttpClient httpClient) {
		Assert.notNull(httpClient, "httpClient must not be null");
		this.httpClient = httpClient;
	}

	/**
	 * Create a new instance with the state of the given
	 * {@link HttpComponents5ClientFactory}.
	 * @param factory the factory to use
	 * @throws Exception if the client fails to build
	 */
	public SimpleHttpComponents5MessageSender(HttpComponents5ClientFactory factory) throws Exception {
		this(factory.getObject());
	}

	@Override
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

}
