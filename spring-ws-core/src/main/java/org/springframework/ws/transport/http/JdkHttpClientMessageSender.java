/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses the standard Java
 * {@code HttpClient} facilities to execute POST requests.
 * <p>
 * Can be used with a simple default configured {@code HttpClient} or can be constructed
 * with a pre-configured {@code HttpClient}.
 *
 * @author Marten Deinum
 * @since 4.0
 * @see java.net.http.HttpClient
 */
public class JdkHttpClientMessageSender extends AbstractHttpWebServiceMessageSender implements InitializingBean {

	private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(60);

	private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);

	private @Nullable HttpClient httpClient;

	private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;

	public JdkHttpClientMessageSender() {
	}

	public JdkHttpClientMessageSender(HttpClient httpClient) {

		Assert.notNull(httpClient, "httpClient must not be null");
		this.httpClient = httpClient;
	}

	public void setHttpClient(@Nullable HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setConnectionTimeout(Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setRequestTimeout(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		Assert.state(this.httpClient != null, "HttpClient is not available");
		JdkHttpClientConnection connection = new JdkHttpClientConnection(this.httpClient, uri, this.requestTimeout);

		if (isAcceptGzipEncoding()) {
			connection.addRequestHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}

		return connection;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (this.httpClient == null) {
			this.httpClient = HttpClient.newBuilder().connectTimeout(this.connectionTimeout).build();
		}
	}

}
