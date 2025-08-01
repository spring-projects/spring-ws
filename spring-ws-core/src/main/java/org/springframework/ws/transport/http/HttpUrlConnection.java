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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of the {@link WebServiceConnection} interface that uses a
 * {@link HttpURLConnection}.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @author Oddgeir Gitlestad
 * @since 1.0.0
 */
public class HttpUrlConnection extends AbstractHttpSenderConnection {

	private final HttpURLConnection connection;

	/**
	 * Creates a new instance of the {@code HttpUrlConnection} with the given
	 * {@code HttpURLConnection}.
	 * @param connection the {@code HttpURLConnection}
	 */
	protected HttpUrlConnection(HttpURLConnection connection) {
		Assert.notNull(connection, "connection must not be null");
		this.connection = connection;
	}

	public HttpURLConnection getConnection() {
		return this.connection;
	}

	@Override
	public void onClose() {
		this.connection.disconnect();
	}

	/*
	 * URI
	 */

	@Override
	public URI getUri() throws URISyntaxException {
		return new URI(StringUtils.replace(this.connection.getURL().toString(), " ", "%20"));
	}

	/*
	 * Sending request
	 */

	@Override
	public void addRequestHeader(String name, String value) throws IOException {
		this.connection.addRequestProperty(name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return this.connection.getOutputStream();
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		this.connection.connect();
	}

	/*
	 * Receiving response
	 */

	@Override
	protected long getResponseContentLength() throws IOException {
		return this.connection.getContentLength();
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		Set<String> headerNames = new HashSet<>();
		// Header field 0 is the status line, so we start at 1
		int i = 1;
		while (true) {
			String headerName = this.connection.getHeaderFieldKey(i);
			if (!StringUtils.hasLength(headerName)) {
				break;
			}
			headerNames.add(headerName);
			i++;
		}
		return headerNames.iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		Map<String, List<String>> headersListMappedByLowerCaseName = new HashMap<>();

		for (String key : this.connection.getHeaderFields().keySet()) {
			if (key != null) {
				headersListMappedByLowerCaseName.put(key.toLowerCase(), this.connection.getHeaderFields().get(key));
			}
		}

		List<String> headerValues = headersListMappedByLowerCaseName.get(name.toLowerCase());

		if (headerValues == null) {
			return Collections.emptyIterator();
		}
		else {
			return headerValues.iterator();
		}
	}

	@Override
	protected int getResponseCode() throws IOException {
		return this.connection.getResponseCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return this.connection.getResponseMessage();
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		if (this.connection.getResponseCode() / 100 != 2) {
			return this.connection.getErrorStream();
		}
		else {
			return this.connection.getInputStream();
		}
	}

}
