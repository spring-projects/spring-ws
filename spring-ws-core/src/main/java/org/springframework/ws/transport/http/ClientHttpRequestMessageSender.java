/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
 * {@code WebServiceMessageSender} implementation based on the
 * {@link ClientHttpRequestFactory} introduced in Spring 3.
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
		this.requestFactory = requestFactory;
	}

	public ClientHttpRequestFactory getRequestFactory() {
		return this.requestFactory;
	}

	public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		Assert.notNull(requestFactory, "'requestFactory' must not be null");
		this.requestFactory = requestFactory;
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		ClientHttpRequest request = this.requestFactory.createRequest(uri, HttpMethod.POST);
		if (isAcceptGzipEncoding()) {
			request.getHeaders()
				.add(HttpTransportConstants.HEADER_ACCEPT_ENCODING, HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		return new ClientHttpRequestConnection(request);
	}

}
