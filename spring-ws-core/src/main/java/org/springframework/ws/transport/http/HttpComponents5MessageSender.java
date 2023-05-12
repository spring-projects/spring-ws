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

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * {@code WebServiceMessageSender} implementation that uses <a href="http://hc.apache.org/httpcomponents-client">Apache
 * HttpClient</a> to execute POST requests.
 * <p>
 * Allows to use a pre-configured HttpClient instance, potentially with authentication, HTTP connection pooling, etc.
 * Authentication can also be set by injecting a {@link Credentials} instance (such as the
 * {@link org.apache.hc.client5.http.auth.UsernamePasswordCredentials}).
 *
 * @author Alan Stewart
 * @author Barry Pitman
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @author Lars Uffmann
 * @see HttpClient
 * @since 4.0.5
 */
public class HttpComponents5MessageSender extends AbstractHttpWebServiceMessageSender
		implements InitializingBean, DisposableBean {

	private static final String HTTP_CLIENT_ALREADY_SET = "httpClient already set";

	private HttpClient httpClient;

	private HttpComponents5ClientFactory clientFactory;

	/**
	 * Create a new instance of the {@code HttpClientMessageSender} with a default {@link HttpClient} that uses a default
	 * {@link PoolingHttpClientConnectionManager}.
	 */
	public HttpComponents5MessageSender() {

		this.clientFactory = new HttpComponents5ClientFactory();
		this.clientFactory.setClientBuilderCustomizer(
				httpClientBuilder -> httpClientBuilder.addRequestInterceptorFirst(new RemoveSoapHeadersInterceptor()));
	}

	/**
	 * Create a new instance of the {@link HttpComponents5MessageSender} with the given {@link HttpClient} instance.
	 * <p>
	 * This constructor does not change the given {@code HttpClient} in any way. As such, it does not set timeouts, nor
	 * does it {@linkplain HttpClientBuilder#addRequestInterceptorFirst(HttpRequestInterceptor) add} the
	 * {@link RemoveSoapHeadersInterceptor}.
	 *
	 * @param httpClient the HttpClient instance to use for this sender
	 */
	public HttpComponents5MessageSender(HttpClient httpClient) {

		Assert.notNull(httpClient, "httpClient must not be null");
		this.httpClient = httpClient;
	}

	/**
	 * @see HttpComponents5ClientFactory#setAuthScope(AuthScope)
	 */
	public void setAuthScope(AuthScope authScope) {

		if (getHttpClient() != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}

		this.clientFactory.setAuthScope(authScope);
	}

	/**
	 * @see HttpComponents5ClientFactory#setCredentials(Credentials)
	 */
	public void setCredentials(Credentials credentials) {

		if (getHttpClient() != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}

		this.clientFactory.setCredentials(credentials);
	}

	/**
	 * Returns the {@code HttpClient} used by this message sender.
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Set the {@code HttpClient} used by this message sender.
	 */
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * @see HttpComponents5ClientFactory#setConnectionTimeout(Duration)
	 */
	public void setConnectionTimeout(Duration timeout) {

		if (getHttpClient() != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}

		this.clientFactory.setConnectionTimeout(timeout);
	}

	/**
	 * @see HttpComponents5ClientFactory#setReadTimeout(Duration)
	 */
	public void setReadTimeout(Duration timeout) {

		if (getHttpClient() != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}

		this.clientFactory.setReadTimeout(timeout);
	}

	/**
	 * @see HttpComponents5ClientFactory#setMaxTotalConnections(int)
	 */
	public void setMaxTotalConnections(int maxTotalConnections) {

		if (getHttpClient() != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}

		this.clientFactory.setMaxTotalConnections(maxTotalConnections);
	}

	/**
	 * @see HttpComponents5ClientFactory#setMaxConnectionsPerHost(Map)
	 */
	public void setMaxConnectionsPerHost(Map<String, String> maxConnectionsPerHost) {

		if (getHttpClient() != null) {
			throw new IllegalStateException(HTTP_CLIENT_ALREADY_SET);
		}

		this.clientFactory.setMaxConnectionsPerHost(maxConnectionsPerHost);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.httpClient = clientFactory.getObject();
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {

		HttpPost httpPost = new HttpPost(uri);

		if (isAcceptGzipEncoding()) {
			httpPost.addHeader(HttpTransportConstants.HEADER_ACCEPT_ENCODING, HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}

		HttpContext httpContext = createContext(uri);

		return new HttpComponents5Connection(getHttpClient(), httpPost, httpContext);
	}

	/**
	 * Template method that allows for creation of an {@link HttpContext} for the given uri. Default implementation returns
	 * {@code null}.
	 *
	 * @param uri the URI to create the context for
	 * @return the context, or {@code null}
	 */
	protected HttpContext createContext(URI uri) {
		return null;
	}

	@Override
	public void destroy() throws Exception {

		if (getHttpClient()instanceof CloseableHttpClient client) {
			client.close();
		}
	}

	/**
	 * HttpClient {@link HttpRequestInterceptor} implementation that removes {@code Content-Length} and
	 * {@code Transfer-Encoding} headers from the request. Necessary, because some SAAJ and other SOAP implementations set
	 * these headers themselves, and HttpClient throws an exception if they have been set.
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
}
