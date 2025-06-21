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
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Apache HttpClient 5.
 * Exposes a {@link HttpPost} and {@link HttpResponse}.
 *
 * @author Alan Stewart
 * @author Barry Pitman
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @author Lars Uffmann
 * @since 4.0.5
 */
public class HttpComponents5Connection extends AbstractHttpSenderConnection {

	private final HttpClient httpClient;

	private final HttpPost httpPost;

	private final HttpContext httpContext;

	private HttpResponse httpResponse;

	private ByteArrayOutputStream requestBuffer;

	protected HttpComponents5Connection(HttpClient httpClient, HttpPost httpPost, HttpContext httpContext) {

		Assert.notNull(httpClient, "httpClient must not be null");
		Assert.notNull(httpPost, "httpPost must not be null");

		this.httpClient = httpClient;
		this.httpPost = httpPost;
		this.httpContext = httpContext;
	}

	public HttpPost getHttpPost() {
		return this.httpPost;
	}

	public HttpResponse getHttpResponse() {
		return this.httpResponse;
	}

	@Override
	public void onClose() throws IOException {

		if (this.httpResponse instanceof ClassicHttpResponse response) {

			if (response.getEntity() != null) {
				EntityUtils.consume(response.getEntity());
			}
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
		return this.requestBuffer;
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {

		String contentType = this.httpPost.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
		this.httpPost.setEntity(new ByteArrayEntity(this.requestBuffer.toByteArray(), ContentType.parse(contentType)));

		this.requestBuffer = null;

		if (this.httpContext != null) {
			this.httpResponse = this.httpClient.execute(this.httpPost, this.httpContext);
		}
		else {
			this.httpResponse = this.httpClient.execute(this.httpPost);
		}
	}

	/*
	 * Receiving response
	 */

	@Override
	protected int getResponseCode() throws IOException {
		return this.httpResponse.getCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return this.httpResponse.getReasonPhrase();
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

		return Arrays.stream(this.httpResponse.getHeaders()).map(NameValuePair::getName).iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {

		return Arrays.stream(this.httpResponse.getHeaders(name)).map(NameValuePair::getValue).iterator();
	}

}
