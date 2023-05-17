/*
 * Copyright 2005-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
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
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.FactoryBean;

/**
 * {@link FactoryBean} to set up a {@link CloseableHttpClient} using HttpComponents HttpClient 5.
 *
 * @see http://hc.apache.org/httpcomponents-client
 * @author Lars Uffmann
 * @since 4.0.5
 */
public class HttpComponents5ClientFactory implements FactoryBean<CloseableHttpClient> {

	private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(60);

	private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

	private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	private Duration readTimeout = DEFAULT_READ_TIMEOUT;

	private int maxTotalConnections = -1;

	private AuthScope authScope = null;

	private Credentials credentials = null;

	private Map<String, String> maxConnectionsPerHost = Map.of();

	private PoolingHttpClientConnectionManager connectionManager;

	private HttpClientBuilderCustomizer clientBuilderCustomizer;

	private PoolingHttpClientConnectionManagerBuilderCustomizer connectionManagerBuilderCustomizer;

	/**
	 * Sets the credentials to be used. If not set, no authentication is done.
	 *
	 * @see org.apache.hc.client5.http.auth.UsernamePasswordCredentials
	 * @see org.apache.hc.client5.http.auth.NTCredentials
	 */
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	/**
	 * Sets the authentication scope to be used. Only used when the {@code credentials} property has been set.
	 * <p>
	 * By default, the {@link AuthScope#ANY} is used.
	 *
	 * @see #setCredentials(Credentials)
	 */
	public void setAuthScope(AuthScope authScope) {
		this.authScope = authScope;
	}

	/**
	 * Sets the timeout until a connection is established. A value of 0 means <em>never</em> timeout.
	 *
	 * @param timeout the timeout value
	 */
	public void setConnectionTimeout(Duration timeout) {

		if (timeout.isNegative()) {
			throw new IllegalArgumentException("timeout must be a non-negative value");
		}

		this.connectionTimeout = timeout;
	}

	/**
	 * Set the socket read timeout for the underlying HttpClient. A value of 0 means <em>never</em> timeout.
	 *
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
	 *
	 * @param maxTotalConnections the maximum number of connections allowed
	 * @see PoolingHttpClientConnectionManager...
	 */
	public void setMaxTotalConnections(int maxTotalConnections) {

		if (maxTotalConnections <= 0) {
			throw new IllegalArgumentException("maxTotalConnections must be a positive value");
		}

		this.maxTotalConnections = maxTotalConnections;
	}

	/**
	 * Sets the maximum number of connections per host for the underlying HttpClient. The maximum number of connections
	 * per host can be set in a form accepted by the {@code java.util.Properties} class, like as follows:
	 *
	 * <pre>
	 * https://www.example.com=1
	 * http://www.example.com:8080=7
	 * http://www.springframework.org=10
	 * </pre>
	 * <p>
	 * The host can be specified as a URI (with scheme and port).
	 *
	 * @param maxConnectionsPerHost a properties object specifying the maximum number of connection
	 * @see PoolingHttpClientConnectionManager...
	 */
	public void setMaxConnectionsPerHost(Map<String, String> maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	void applyMaxConnectionsPerHost(PoolingHttpClientConnectionManager connectionManager) throws URISyntaxException {

		for (Map.Entry<String, String> entry : maxConnectionsPerHost.entrySet()) {

			URI uri = new URI(entry.getKey());
			HttpHost host = new HttpHost(uri.getScheme(), uri.getHost(), getPort(uri));
			final HttpRoute route;

			if (uri.getScheme().equals("https")) {
				route = new HttpRoute(host, null, true);
			} else {
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

		PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder
				.create();

		if (this.maxTotalConnections != -1) {
			connectionManagerBuilder.setMaxConnTotal(this.maxTotalConnections);
		}

		if (this.connectionManagerBuilderCustomizer != null) {
			this.connectionManagerBuilderCustomizer.customize(connectionManagerBuilder);
		}

		this.connectionManager = connectionManagerBuilder.build();

		applyMaxConnectionsPerHost(connectionManager);

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom() //
				.setConnectionRequestTimeout(Timeout.of(connectionTimeout)) //
				.setResponseTimeout(Timeout.of(readTimeout));

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create() //
				.setDefaultRequestConfig(requestConfigBuilder.build()) //
				.setConnectionManager(connectionManager);

		if (credentials != null && authScope != null) {

			BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
			basicCredentialsProvider.setCredentials(authScope, credentials);
			httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
		}

		if (this.clientBuilderCustomizer != null) {
			clientBuilderCustomizer.customize(httpClientBuilder);
		}

		return httpClientBuilder.build();
	}

	@Override
	public Class<?> getObjectType() {
		return CloseableHttpClient.class;
	}

	PoolingHttpClientConnectionManager getConnectionManager() {
		return this.connectionManager;
	}

	public void setClientBuilderCustomizer(HttpClientBuilderCustomizer clientBuilderCustomizer) {
		this.clientBuilderCustomizer = clientBuilderCustomizer;
	}

	public void setConnectionManagerBuilderCustomizer(
			PoolingHttpClientConnectionManagerBuilderCustomizer connectionManagerBuilderCustomizer) {
		this.connectionManagerBuilderCustomizer = connectionManagerBuilderCustomizer;
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
