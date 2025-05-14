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

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

/**
 * {@link FactoryBean} to set up a {@link CloseableHttpClient} using HttpComponents
 * HttpClient 5.
 *
 * @author Lars Uffmann
 * @author Stephane Nicoll
 * @since 4.0.5
 * @see <a href=https://hc.apache.org/httpcomponents-client>HttpComponents</a>
 */
public class HttpComponents5ClientFactory implements FactoryBean<CloseableHttpClient> {

	/**
	 * {@link AuthScope} to match any Host.
	 * <p>
	 * <b>NOTE</b>: {@code ANY} was removed from {@link AuthScope} in HttpComponents 5.0.
	 * This value object will easy migration from HttpComponents 4. Consider using a
	 * {@link ClientInterceptor} to implement http client agnostic preemptive basic auth.
	 * @see AuthScope#AuthScope(String, String, int, String, String)
	 */
	public static final AuthScope ANY = new AuthScope(null, null, -1, null, null);

	private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(60);

	private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

	private final List<HttpClientBuilderCustomizer> clientBuilderCustomizers = new ArrayList<>();

	private final List<PoolingHttpClientConnectionManagerBuilderCustomizer> connectionManagerBuilderCustomizers = new ArrayList<>();

	private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	private Duration readTimeout = DEFAULT_READ_TIMEOUT;

	private int maxTotalConnections = -1;

	private AuthScope authScope = ANY;

	private Credentials credentials = null;

	private Map<String, String> maxConnectionsPerHost = Map.of();

	private PoolingHttpClientConnectionManager connectionManager;

	/**
	 * Create a new instance with default settings. This configures
	 * {@link RemoveSoapHeadersInterceptor} as the first interceptor
	 * @return a factory with default settings
	 */
	public static HttpComponents5ClientFactory withDefaults() {
		HttpComponents5ClientFactory factory = new HttpComponents5ClientFactory();
		factory.addClientBuilderCustomizer((httpClientBuilder) -> httpClientBuilder
			.addRequestInterceptorFirst(new RemoveSoapHeadersInterceptor()));
		return factory;
	}

	/**
	 * Add a {@link HttpClientBuilderCustomizer} to invoke when creating an
	 * {@link CloseableHttpClient} managed by this factory.
	 * @param clientBuilderCustomizer the customizer to invoke
	 * @since 4.1.0
	 */
	public void addClientBuilderCustomizer(HttpClientBuilderCustomizer clientBuilderCustomizer) {
		this.clientBuilderCustomizers.add(clientBuilderCustomizer);
	}

	/**
	 * Add a {@link PoolingHttpClientConnectionManagerBuilderCustomizer} to invoke when
	 * creating an {@link CloseableHttpClient} managed by this factory.
	 * @param connectionManagerBuilderCustomizer the customizer to invoke
	 */
	public void addConnectionManagerBuilderCustomizer(
			PoolingHttpClientConnectionManagerBuilderCustomizer connectionManagerBuilderCustomizer) {
		this.connectionManagerBuilderCustomizers.add(connectionManagerBuilderCustomizer);
	}

	/**
	 * Sets the credentials to be used. If not set, no authentication is done.
	 * @see org.apache.hc.client5.http.auth.UsernamePasswordCredentials
	 * @see org.apache.hc.client5.http.auth.NTCredentials
	 */
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	/**
	 * Sets the authentication scope to be used. Only used when the {@code credentials}
	 * property has been set.
	 * <p>
	 * By default, {@link #ANY} is used.
	 * @see #setCredentials(Credentials)
	 */
	public void setAuthScope(AuthScope authScope) {
		this.authScope = authScope;
	}

	/**
	 * Sets the timeout until a connection is established. A value of 0 means
	 * <em>never</em> timeout.
	 * @param timeout the timeout value
	 */
	public void setConnectionTimeout(Duration timeout) {
		if (timeout.isNegative()) {
			throw new IllegalArgumentException("timeout must be a non-negative value");
		}
		this.connectionTimeout = timeout;
	}

	/**
	 * Set the socket read timeout for the underlying HttpClient. A value of 0 means
	 * <em>never</em> timeout.
	 * @param timeout the timeout value
	 */
	public void setReadTimeout(Duration timeout) {
		if (timeout.isNegative()) {
			throw new IllegalArgumentException("timeout must be a non-negative value");
		}
		this.readTimeout = timeout;
	}

	/**
	 * Sets the maximum number of connections allowed for the underlying HttpClient.
	 * @param maxTotalConnections the maximum number of connections allowed
	 * @see PoolingHttpClientConnectionManager
	 */
	public void setMaxTotalConnections(int maxTotalConnections) {
		if (maxTotalConnections <= 0) {
			throw new IllegalArgumentException("maxTotalConnections must be a positive value");
		}
		this.maxTotalConnections = maxTotalConnections;
	}

	/**
	 * Sets the maximum number of connections per host for the underlying HttpClient. The
	 * maximum number of connections per host can be set in a form accepted by the
	 * {@code java.util.Properties} class, like as follows:
	 *
	 * <pre>
	 * https://www.example.com=1
	 * http://www.example.com:8080=7
	 * http://www.springframework.org=10
	 * </pre>
	 * <p>
	 * The host can be specified as a URI (with scheme and port).
	 * @param maxConnectionsPerHost a properties object specifying the maximum number of
	 * connection
	 * @see PoolingHttpClientConnectionManager
	 */
	public void setMaxConnectionsPerHost(Map<String, String> maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	PoolingHttpClientConnectionManager getConnectionManager() {
		return this.connectionManager;
	}

	@SuppressWarnings("deprecation")
	public CloseableHttpClient build() {
		PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder
			.create();
		if (this.maxTotalConnections != -1) {
			connectionManagerBuilder.setMaxConnTotal(this.maxTotalConnections);
		}
		this.connectionManagerBuilderCustomizers.forEach(customizer -> customizer.customize(connectionManagerBuilder));
		this.connectionManager = connectionManagerBuilder.build();

		applyMaxConnectionsPerHost(this.connectionManager);

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
			.setConnectTimeout(Timeout.of(this.connectionTimeout))
			.setResponseTimeout(Timeout.of(this.readTimeout));

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
			.setDefaultRequestConfig(requestConfigBuilder.build())
			.setConnectionManager(this.connectionManager);

		if (this.credentials != null && this.authScope != null) {
			BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
			basicCredentialsProvider.setCredentials(this.authScope, this.credentials);
			httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
		}

		this.clientBuilderCustomizers.forEach(customizer -> customizer.customize(httpClientBuilder));

		return httpClientBuilder.build();
	}

	void applyMaxConnectionsPerHost(PoolingHttpClientConnectionManager connectionManager) {
		for (Map.Entry<String, String> entry : this.maxConnectionsPerHost.entrySet()) {
			URI uri = URI.create(entry.getKey());
			HttpHost host = new HttpHost(uri.getScheme(), uri.getHost(), getPort(uri));
			final HttpRoute route;

			if (uri.getScheme().equals("https")) {
				route = new HttpRoute(host, null, true);
			}
			else {
				route = new HttpRoute(host);
			}
			int max = Integer.parseInt(entry.getValue());
			connectionManager.setMaxPerRoute(route, max);
		}
	}

	static int getPort(URI uri) {
		if (uri.getPort() == -1) {
			if ("https".equalsIgnoreCase(uri.getScheme())) {
				return 443;
			}
			if ("http".equalsIgnoreCase(uri.getScheme())) {
				return 80;
			}
		}
		return uri.getPort();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public CloseableHttpClient getObject() throws Exception {
		return build();
	}

	@Override
	public Class<?> getObjectType() {
		return CloseableHttpClient.class;
	}

	/**
	 * HttpClient {@link HttpRequestInterceptor} implementation that removes
	 * {@code Content-Length} and {@code Transfer-Encoding} headers from the request.
	 * Necessary, because some SAAJ and other SOAP implementations set these headers
	 * themselves, and HttpClient throws an exception if they have been set.
	 */
	public static class RemoveSoapHeadersInterceptor implements HttpRequestInterceptor {

		@Override
		public void process(HttpRequest request, EntityDetails entityDetails, HttpContext httpContext)
				throws HttpException, IOException {

			if (request.containsHeader(HttpHeaders.TRANSFER_ENCODING)) {
				request.removeHeaders(HttpHeaders.TRANSFER_ENCODING);
			}

			if (request.containsHeader(HttpHeaders.CONTENT_LENGTH)) {
				request.removeHeaders(HttpHeaders.CONTENT_LENGTH);
			}
		}

	}

	@FunctionalInterface
	public interface HttpClientBuilderCustomizer {

		void customize(HttpClientBuilder httpClientBuilder);

	}

	@FunctionalInterface
	public interface PoolingHttpClientConnectionManagerBuilderCustomizer {

		void customize(PoolingHttpClientConnectionManagerBuilder poolingHttpClientConnectionManagerBuilder);

	}

}
