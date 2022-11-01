/*
 * Copyright 2005-2022 the original author or authors.
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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on Jakarta Commons HttpClient. Exposes a
 * {@link PostMethod}.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 * @deprecated In favor of {@link HttpComponentsConnection}
 */
@Deprecated
public class CommonsHttpConnection extends AbstractHttpSenderConnection {

	private final HttpClient httpClient;

	private final PostMethod postMethod;

	private ByteArrayOutputStream requestBuffer;

	private MultiThreadedHttpConnectionManager connectionManager;

	protected CommonsHttpConnection(HttpClient httpClient, PostMethod postMethod) {
		Assert.notNull(httpClient, "httpClient must not be null");
		Assert.notNull(postMethod, "postMethod must not be null");
		this.httpClient = httpClient;
		this.postMethod = postMethod;
	}

	public PostMethod getPostMethod() {
		return postMethod;
	}

	@Override
	public void onClose() throws IOException {
		postMethod.releaseConnection();
		if (connectionManager != null) {
			connectionManager.shutdown();
		}
	}

	/*
	 * URI
	 */

	@Override
	public URI getUri() throws URISyntaxException {
		try {
			return new URI(postMethod.getURI().toString());
		} catch (URIException ex) {
			throw new URISyntaxException("", ex.getMessage());
		}
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
		postMethod.addRequestHeader(name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return requestBuffer;
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		postMethod.setRequestEntity(new ByteArrayRequestEntity(requestBuffer.toByteArray()));
		requestBuffer = null;
		try {
			httpClient.executeMethod(postMethod);
		} catch (IllegalStateException ex) {
			if ("Connection factory has been shutdown.".equals(ex.getMessage())) {
				// The application context has been closed, resulting in a connection factory shutdown and an ISE.
				// Let's create a new connection factory for this connection only.
				connectionManager = new MultiThreadedHttpConnectionManager();
				httpClient.setHttpConnectionManager(connectionManager);
				httpClient.executeMethod(postMethod);
			} else {
				throw ex;
			}
		}
	}

	/*
	 * Receiving response
	 */

	@Override
	protected int getResponseCode() throws IOException {
		return postMethod.getStatusCode();
	}

	@Override
	protected String getResponseMessage() throws IOException {
		return postMethod.getStatusText();
	}

	@Override
	protected long getResponseContentLength() throws IOException {
		return postMethod.getResponseContentLength();
	}

	@Override
	protected InputStream getRawResponseInputStream() throws IOException {
		return postMethod.getResponseBodyAsStream();
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		Header[] headers = postMethod.getResponseHeaders();
		String[] names = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			names[i] = headers[i].getName();
		}
		return Arrays.asList(names).iterator();
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		Header[] headers = postMethod.getResponseHeaders(name);
		String[] values = new String[headers.length];
		for (int i = 0; i < headers.length; i++) {
			values[i] = headers[i].getValue();
		}
		return Arrays.asList(values).iterator();
	}

}
