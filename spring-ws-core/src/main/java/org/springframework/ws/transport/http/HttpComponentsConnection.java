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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Apache HttpClient.
 * Exposes a {@link HttpPost} and {@link HttpResponse}.
 *
 * @author Alan Stewart
 * @author Barry Pitman
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 2.1.0
 */
public class HttpComponentsConnection extends AbstractHttpSenderConnection {

	private final HttpClient httpClient;

	private final HttpPost httpPost;

	private final @Nullable HttpContext httpContext;

	private @Nullable HttpResponse httpResponse;

	private @Nullable ByteArrayOutputStream requestBuffer;

	protected HttpComponentsConnection(HttpClient httpClient, HttpPost httpPost, @Nullable HttpContext httpContext) {
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
		Assert.notNull(this.httpResponse, "HttpResponse is not available");
		return this.httpResponse;
	}

	@Override
	public void onClose() throws IOException {
		if (this.httpResponse != null && this.httpResponse.getEntity() != null) {
			EntityUtils.consume(this.httpResponse.getEntity());
		}
	}

	/*
	 * URI
	 */
	@Override
	public URI getUri() throws URISyntaxException {
		return new URI(this.httpPost.getURI().toString());
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
		Assert.state(this.requestBuffer != null, "onSendBeforeWrite has not been called");
		this.httpPost.setEntity(new ByteArrayEntity(this.requestBuffer.toByteArray()));
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
		return getHttpResponse().getStatusLine().getStatusCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return getHttpResponse().getStatusLine().getReasonPhrase();
	}

	@Override
	protected long getResponseContentLength() throws IOException {
		HttpEntity entity = getHttpResponse().getEntity();
		if (entity != null) {
			return entity.getContentLength();
		}
		return 0;
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		HttpEntity entity = getHttpResponse().getEntity();
		if (entity != null) {
			return entity.getContent();
		}
		throw new IllegalStateException("Response has no enclosing response entity, cannot create input stream");
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		Header[] headers = getHttpResponse().getAllHeaders();
		String[] names = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			names[i] = headers[i].getName();
		}
		return Arrays.asList(names).iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		Header[] headers = getHttpResponse().getHeaders(name);
		String[] values = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			values[i] = headers[i].getValue();
		}
		return Arrays.asList(values).iterator();
	}

}
