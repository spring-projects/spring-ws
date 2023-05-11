/*
 * Copyright 2023-2023 the original author or authors.
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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses the standard Java {@code HttpClient} facilities to execute
 * POST requests.
 * <p>
 * Can be used with a simple default configured {@code HttpClient} or can be constructed with a pre-configured
 * {@code HttpClient}.
 *
 * @author Marten Deinum
 * @see java.net.http.HttpClient
 * @since 4.0
 */
public class JdkHttpClientMessageSender extends AbstractHttpWebServiceMessageSender implements InitializingBean {

	private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(60);

	private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(60);

	private HttpClient httpClient;

	private Duration connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

	private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;

	public JdkHttpClientMessageSender() {}

	public JdkHttpClientMessageSender(HttpClient httpClient) {

		Assert.notNull(httpClient, "httpClient must not be null");
		this.httpClient = httpClient;
	}

	public void setHttpClient(@Nullable HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setConnectionTimeout(@Nullable Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setRequestTimeout(@Nullable Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {

		JdkHttpClientConnection connection = new JdkHttpClientConnection(httpClient, uri, requestTimeout);

		if (isAcceptGzipEncoding()) {
			connection.addRequestHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}

		return connection;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (httpClient == null) {
			httpClient = HttpClient.newBuilder().connectTimeout(connectionTimeout).build();
		}
	}
}
