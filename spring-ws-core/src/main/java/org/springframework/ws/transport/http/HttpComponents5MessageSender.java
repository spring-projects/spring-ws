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

import java.time.Duration;
import java.util.Map;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpRequestInterceptor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.http.HttpComponentsMessageSender.RemoveSoapHeadersInterceptor;

/**
 * {@code AbstractHttpComponents5MessageSender} implementation that configures the
 * underlying <a href="http://hc.apache.org/httpcomponents-client">Apache HttpClient</a>
 * that executes POST requests.
 * <p>
 * To specify the {@link HttpClient}, consider using
 * {@link SimpleHttpComponents5MessageSender} instead.
 *
 * @author Alan Stewart
 * @author Barry Pitman
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @author Lars Uffmann
 * @since 4.0.5
 * @see HttpClient
 */
public class HttpComponents5MessageSender extends AbstractHttpComponents5MessageSender implements InitializingBean {

	private static final String HTTP_CLIENT_ALREADY_SET = "httpClient already set";

	private final HttpComponents5ClientFactory clientFactory;

	private HttpClient httpClient;

	/**
	 * Create a new instance of the {@code HttpClientMessageSender} with a default
	 * {@link HttpClient} that uses a default {@link PoolingHttpClientConnectionManager}.
	 */
	public HttpComponents5MessageSender() {
		this.clientFactory = HttpComponents5ClientFactory.withDefaults();
	}

	/**
	 * Create a new instance of the {@link HttpComponents5MessageSender} with the given
	 * {@link HttpClient} instance.
	 * <p>
	 * This constructor does not change the given {@code HttpClient} in any way. As such,
	 * it does not set timeouts, nor does it
	 * {@linkplain HttpClientBuilder#addRequestInterceptorFirst(HttpRequestInterceptor)
	 * add} the {@link RemoveSoapHeadersInterceptor}.
	 * @param httpClient the HttpClient instance to use for this sender
	 * @deprecated as of 4.1.0 in favor of {@link SimpleHttpComponents5MessageSender}
	 */
	@Deprecated(since = "4.1.0", forRemoval = true)
	public HttpComponents5MessageSender(HttpClient httpClient) {
		this();
		Assert.notNull(httpClient, "httpClient must not be null");
		this.httpClient = httpClient;
	}

	@Override
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	/**
	 * Set the authentication scope to be used. Only used when the {@code credentials}
	 * property has been set.
	 * @see HttpComponents5ClientFactory#setAuthScope(AuthScope)
	 */
	public void setAuthScope(AuthScope authScope) {
		if (this.httpClient != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}
		this.clientFactory.setAuthScope(authScope);
	}

	/**
	 * Set the credentials to be used. If not set, no authentication is done.
	 * @see HttpComponents5ClientFactory#setCredentials(Credentials)
	 */
	public void setCredentials(Credentials credentials) {
		if (this.httpClient != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}
		this.clientFactory.setCredentials(credentials);
	}

	/**
	 * Set the {@code HttpClient} used by this message sender.
	 * <p>
	 * This effectively disable any customization and does not change the given
	 * {@code HttpClient} in any way. As such, it does not set timeouts, nor does it
	 * {@linkplain HttpClientBuilder#addRequestInterceptorFirst(HttpRequestInterceptor)
	 * add} the {@link RemoveSoapHeadersInterceptor}.
	 * @param httpClient the HttpClient to use
	 * @deprecated as of 4.1.0 in favor of {@link SimpleHttpComponents5MessageSender}
	 */
	@Deprecated(since = "4.1.0", forRemoval = true)
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Set the timeout until a connection is established.
	 * @see HttpComponents5ClientFactory#setConnectionTimeout(Duration)
	 */
	public void setConnectionTimeout(Duration timeout) {
		if (this.httpClient != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}
		this.clientFactory.setConnectionTimeout(timeout);
	}

	/**
	 * Set the socket read timeout for the underlying HttpClient.
	 * @see HttpComponents5ClientFactory#setReadTimeout(Duration)
	 */
	public void setReadTimeout(Duration timeout) {
		if (this.httpClient != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}
		this.clientFactory.setReadTimeout(timeout);
	}

	/**
	 * Sets the maximum number of connections allowed for the underlying HttpClient.
	 * @see HttpComponents5ClientFactory#setMaxTotalConnections(int)
	 */
	public void setMaxTotalConnections(int maxTotalConnections) {
		if (this.httpClient != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}
		this.clientFactory.setMaxTotalConnections(maxTotalConnections);
	}

	/**
	 * Sets the maximum number of connections per host for the underlying HttpClient.
	 * @see HttpComponents5ClientFactory#setMaxConnectionsPerHost(Map)
	 */
	public void setMaxConnectionsPerHost(Map<String, String> maxConnectionsPerHost) {
		if (this.httpClient != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}
		this.clientFactory.setMaxConnectionsPerHost(maxConnectionsPerHost);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.httpClient == null) {
			this.httpClient = this.clientFactory.getObject();
		}
	}

}
