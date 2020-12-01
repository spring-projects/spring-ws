/*
 * Copyright 2005-2014 the original author or authors.
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

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation based on the {@link ClientHttpRequestFactory} introduced in Spring 3.
 *
 * @author Krzysztof Trojan
 * @author Arjen Poutsma
 * @since 2.2
 */
public class ClientHttpRequestMessageSender extends AbstractHttpWebServiceMessageSender {

	private ClientHttpRequestFactory requestFactory;

	public ClientHttpRequestMessageSender() {
		this(new SimpleClientHttpRequestFactory());
	}

	public ClientHttpRequestMessageSender(ClientHttpRequestFactory requestFactory) {
		setRequestFactory(requestFactory);
	}

	public ClientHttpRequestFactory getRequestFactory() {
		return requestFactory;
	}

	public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		Assert.notNull(requestFactory, "'requestFactory' must not be null");
		this.requestFactory = requestFactory;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		ClientHttpRequest request = requestFactory.createRequest(uri, HttpMethod.POST);
		if (isAcceptGzipEncoding()) {
			request.getHeaders().add(HttpTransportConstants.HEADER_ACCEPT_ENCODING,
					HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		return new ClientHttpRequestConnection(request);
	}
}
