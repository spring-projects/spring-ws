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
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Apache HttpClient. Exposes a {@link org.apache.hc.client5.http.classic.methods.HttpPost} and
 * {@link org.apache.hc.core5.http.HttpResponse
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
		return httpPost;
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	@Override
	public void onClose() throws IOException {
		//XXX:
		if (httpResponse instanceof ClassicHttpResponse response) {
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
		return new URI(httpPost.getUri().toString());
	}

	/*
	  * Sending request
	  */

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		requestBuffer = new ByteArrayOutputStream();
	}

	@Override
	public void addRequestHeader(String name, String value) throws IOException {
		httpPost.addHeader(name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return requestBuffer;
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		//XXX
		httpPost.setEntity(new ByteArrayEntity(requestBuffer.toByteArray(), null));
		requestBuffer = null;
		if (httpContext != null) {
			httpResponse = httpClient.execute(httpPost, httpContext);
		} else {
			httpResponse = httpClient.execute(httpPost);
		}
	}

	/*
	 * Receiving response
	 */

	@Override
	protected int getResponseCode() throws IOException {
		return httpResponse.getCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return httpResponse.getReasonPhrase();
	}

	@Override
	protected long getResponseContentLength() throws IOException {
		//XXX:
		if (httpResponse instanceof ClassicHttpResponse response) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContentLength();
			}
		}
		return 0;
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		if (httpResponse instanceof ClassicHttpResponse response) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContent();
			}
		}
		throw new IllegalStateException("Response has no enclosing response entity, cannot create input stream");
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		Header[] headers = httpResponse.getHeaders();
		String[] names = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			names[i] = headers[i].getName();
		}
		return Arrays.asList(names).iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		Header[] headers = httpResponse.getHeaders(name);
		String[] values = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			values[i] = headers[i].getValue();
		}
		return Arrays.asList(values).iterator();
	}
}
