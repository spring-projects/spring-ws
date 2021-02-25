/*
 * Copyright 2005-2021 the original author or authors.
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
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on OkHttp.
 *
 * @author Mathias Geat
 * @since 3.1.0
 */
public class OkHttpConnection extends AbstractHttpSenderConnection {

	private final OkHttpClient okHttpClient;
	private final URI uri;

	private final MediaType requestMediaType;
	private final Request.Builder requestBuilder;
	private ByteArrayOutputStream requestBuffer;

	private Response response;
	private InputStream responseBuffer;

	public OkHttpConnection(OkHttpClient okHttpClient, URI uri, MediaType requestMediaType) {
		Assert.notNull(okHttpClient, "okHttpClient must not be null");
		Assert.notNull(uri, "uri must not be null");
		Assert.notNull(requestMediaType, "requestMediaType must not be null");

		this.okHttpClient = okHttpClient;
		this.uri = uri;
		this.requestMediaType = requestMediaType;
		this.requestBuilder = new Request.Builder();
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) {
		requestBuffer = new ByteArrayOutputStream();
	}

	@Override
	public void addRequestHeader(String name, String value) {
		requestBuilder.header(name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() {
		return requestBuffer;
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		requestBuilder
				.url(uri.toURL())
				.post(RequestBody.create(requestBuffer.toByteArray(), requestMediaType));

		requestBuffer = null;

		response = okHttpClient.newCall(requestBuilder.build()).execute();

		ResponseBody responseBody = response.body();
		if (responseBody != null) {
			this.responseBuffer = responseBody.byteStream();
		}
	}

	@Override
	protected int getResponseCode() {
		return response.code();
	}

	@Override
	protected String getResponseMessage() {
		return response.message();
	}

	@Override
	protected long getResponseContentLength() {
		ResponseBody responseBody = response.body();
		if (responseBody != null) {
			return responseBody.contentLength();
		}

		return 0;
	}

	@Override
	protected InputStream getRawResponseInputStream() {
		if (responseBuffer == null) {
			throw new IllegalStateException("Response has no response body, cannot create input stream");
		}

		return responseBuffer;
	}

	@Override
	public Iterator<String> getResponseHeaderNames() {
		return response.headers().names().iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) {
		return response.headers(name).iterator();
	}

	@Override
	public void onClose() throws IOException {
		if (responseBuffer != null) {
			responseBuffer.close();
		}
	}
}
