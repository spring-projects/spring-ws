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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of the {@link WebServiceConnection} interface that uses Java's built-in {@link HttpClient}.
 *
 * @author Marten Deinum
 * @see java.net.http.HttpClient
 * @see java.net.http.HttpRequest
 * @since 4.0
 */
public class JdkHttpClientConnection extends AbstractHttpSenderConnection {

	private static final Log logger = LogFactory.getLog(JdkHttpClientConnection.class);

	private static final List<String> DISALLOWED_HEADERS = List.of("connection", "content-length", "expect", "host",
			"upgrade");

	private final HttpClient httpClient;

	private final URI uri;

	private final Builder requestBuilder;

	private HttpRequest request;

	private ByteArrayOutputStream requestBuffer;

	private HttpResponse<InputStream> response;

	protected JdkHttpClientConnection(HttpClient httpClient, URI uri, Duration requestTimeout) {

		Assert.notNull(httpClient, "httpClient must not be null");
		Assert.notNull(uri, "uri must not be null");
		Assert.notNull(requestTimeout, "requestTimeout must not be null");

		this.httpClient = httpClient;
		this.uri = uri;
		this.requestBuilder = HttpRequest.newBuilder(uri).timeout(requestTimeout);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return requestBuffer;
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		return response.headers().map().keySet().iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		return response.headers().allValues(name).iterator();
	}

	@Override
	public void addRequestHeader(String name, String value) throws IOException {

		if (DISALLOWED_HEADERS.contains(name.toLowerCase())) {
			logger.trace("HttpClient doesn't allow setting the '" + name + "' header, ignoring!");
			return;
		}

		requestBuilder.header(name, value);
	}

	@Override
	public URI getUri() throws URISyntaxException {
		return uri;
	}

	@Override
	protected int getResponseCode() throws IOException {
		return response != null ? response.statusCode() : 0;
	}

	@Override
	protected String getResponseMessage() throws IOException {

		HttpStatus status = HttpStatus.resolve(getResponseCode());

		return status != null //
				? status.getReasonPhrase() //
				: "";
	}

	@Override
	protected long getResponseContentLength() throws IOException {

		if (response != null) {

			return response.headers() //
					.firstValueAsLong(HttpTransportConstants.HEADER_CONTENT_LENGTH) //
					.orElse(-1);
		}

		return 0;
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		return response.body();
	}

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		requestBuffer = new ByteArrayOutputStream();
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {

		byte[] body = requestBuffer.toByteArray();

		request = requestBuilder.POST(BodyPublishers.ofByteArray(body)).build();

		try {
			response = httpClient.send(request, BodyHandlers.ofInputStream());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(ex);
		}
	}

	@Override
	protected void onClose() throws IOException {

		if (response != null) {
			response.body().close();
		}
	}
}
