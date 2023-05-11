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
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses the standard Java {@code HttpClient}
 * facilities to execute POST requests.
 * <p>
 * Can be used with a simple default configured {@code HttpClient} or can be constructed with a
 * pre-configured {@code HttpClient}.
 *
 * @author Marten Deinum
 * @see java.net.http.HttpClient
 * @since 4.1
 */
public class JdkHttpClientMessageSender extends AbstractHttpWebServiceMessageSender
		implements InitializingBean {

	private Duration connectionTimeout = Duration.ofSeconds(60);
	private Duration requestTimeout = Duration.ofSeconds(60);

	private HttpClient client;

	public JdkHttpClientMessageSender() {}

	public JdkHttpClientMessageSender(HttpClient client) {
		this.client = client;
	}

	public void setConnectionTimeout(Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setRequestTimeout(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {

		JdkHttpClientConnection connection =
				new JdkHttpClientConnection(this.client, uri, requestTimeout);
		if (isAcceptGzipEncoding()) {
			connection.addRequestHeader(
					HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		return connection;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.client == null) {
			this.client = HttpClient.newBuilder()
					.connectTimeout(this.connectionTimeout)
					.build();
		}
	}
}
