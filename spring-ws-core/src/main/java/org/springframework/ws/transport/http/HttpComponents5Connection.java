/*
 * Copyright 2005-present the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Apache HttpClient 5.
 * Exposes the {@linkplain #getHttpHost() HTTP host}, {@linkplain #getHttpPost() HTTP
 * port}, and {@linkplain #getHttpResponse() HTTP response}.
 *
 * @author Alan Stewart
 * @author Barry Pitman
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @author Lars Uffmann
 * @author Brian Clozel
 * @since 4.0.5
 */
public class HttpComponents5Connection extends AbstractHttpSenderConnection {

	private final HttpClient httpClient;

	private final HttpHost httpHost;

	private final HttpPost httpPost;

	private final @Nullable HttpContext httpContext;

	private @Nullable HttpResponse httpResponse;

	private @Nullable ByteArrayOutputStream requestBuffer;

	protected HttpComponents5Connection(HttpClient httpClient, HttpHost httpHost, HttpPost httpPost,
			@Nullable HttpContext httpContext) {

		Assert.notNull(httpClient, "httpClient must not be null");
		Assert.notNull(httpHost, "httpHost must not be null");
		Assert.notNull(httpPost, "httpPost must not be null");

		this.httpClient = httpClient;
		this.httpHost = httpHost;
		this.httpPost = httpPost;
		this.httpContext = httpContext;
	}

	public HttpHost getHttpHost() {
		return this.httpHost;
	}

	public HttpPost getHttpPost() {
		return this.httpPost;
	}

	public HttpResponse getHttpResponse() {
		Assert.notNull(this.httpResponse, "HttpResponse is not available");
		return this.httpResponse;
	}

	@Override
	public void onClose() throws IOException {
		if (this.httpResponse instanceof ClassicHttpResponse response) {
			if (response.getEntity() != null) {
				EntityUtils.consume(response.getEntity());
			}
			response.close();
		}
	}

	/*
	 * URI
	 */
	@Override
	public URI getUri() throws URISyntaxException {
		return new URI(this.httpPost.getUri().toString());
	}

	/*
	 * Sending request
	 */

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		this.requestBuffer = new ByteArrayOutputStream();
	}

	@Override
	public void addRequestHeader(String name, String value) throws IOException {
		this.httpPost.addHeader(name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		Assert.notNull(this.requestBuffer, "Request OutputStream is not available");
		return this.requestBuffer;
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		String contentType = this.httpPost.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
		Assert.state(this.requestBuffer != null, "onSendBeforeWrite has not been called");
		this.httpPost.setEntity(new ByteArrayEntity(this.requestBuffer.toByteArray(), ContentType.parse(contentType)));
		this.requestBuffer = null;
		this.httpResponse = this.httpClient.executeOpen(this.httpHost, this.httpPost, this.httpContext);
	}

	/*
	 * Receiving response
	 */

	@Override
	protected int getResponseCode() throws IOException {
		return getHttpResponse().getCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return getHttpResponse().getReasonPhrase();
	}

	@Override
	protected long getResponseContentLength() throws IOException {

		if (this.httpResponse instanceof ClassicHttpResponse response) {

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContentLength();
			}
		}
		return 0;
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {

		if (this.httpResponse instanceof ClassicHttpResponse response) {

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContent();
			}
		}

		throw new IllegalStateException("Response has no enclosing response entity, cannot create input stream");
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {

		return Arrays.stream(getHttpResponse().getHeaders()).map(NameValuePair::getName).iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {

		return Arrays.stream(getHttpResponse().getHeaders(name)).map(NameValuePair::getValue).iterator();
	}

}
