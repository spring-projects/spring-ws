/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
	 */
	public SimpleHttpComponents5MessageSender(HttpComponents5ClientFactory factory) {
		this(factory.build());
	}

	@Override
	public HttpClient getHttpClient() {
		return this.httpClient;
	}

}
