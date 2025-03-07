/*
 * Copyright 2005-2025 the original author or authors.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of the {@link WebServiceConnection} interface that is based on the
 * Spring 3 {@link ClientHttpRequest} and {@link ClientHttpResponse}.
 *
 * @author Krzysztof Trojan
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 2.2
 */
public class ClientHttpRequestConnection extends AbstractHttpSenderConnection {

	private final ClientHttpRequest request;

	private ClientHttpResponse response;

	public ClientHttpRequestConnection(ClientHttpRequest request) {
		Assert.notNull(request, "'request' must not be null");
		this.request = request;
	}

	public ClientHttpRequest getClientHttpRequest() {
		return this.request;
	}

	public ClientHttpResponse getClientHttpResponse() {
		return this.response;
	}

	// URI

	@Override
	public URI getUri() throws URISyntaxException {
		return this.request.getURI();
	}

	// Sending request

	@Override
	public void addRequestHeader(String name, String value) throws IOException {
		this.request.getHeaders().add(name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return this.request.getBody();
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		this.response = this.request.execute();
	}

	// Receiving response

	@Override
	protected long getResponseContentLength() throws IOException {
		return this.response.getHeaders().getContentLength();
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		return this.response.getHeaders().keySet().iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		List<String> headers = this.response.getHeaders().get(name);
		return headers != null ? headers.iterator() : Collections.emptyIterator();
	}

	@Override
	protected int getResponseCode() throws IOException {
		return this.response.getStatusCode().value();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return this.response.getStatusText();
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		return this.response.getBody();
	}

	@Override
	protected void onClose() throws IOException {
		if (this.response != null) {
			this.response.close();
		}
	}

}
