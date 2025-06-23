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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.net.httpserver.HttpExchange;
import jakarta.xml.soap.SOAPConstants;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.EndpointAwareWebServiceConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Implementation of {@link WebServiceConnection} that is based on the Java 6 HttpServer
 * {@link HttpExchange}.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */
public class HttpExchangeConnection extends AbstractReceiverConnection
		implements EndpointAwareWebServiceConnection, FaultAwareWebServiceConnection {

	private final HttpExchange httpExchange;

	private @Nullable ByteArrayOutputStream responseBuffer;

	private int responseStatusCode = HttpTransportConstants.STATUS_ACCEPTED;

	private boolean chunkedEncoding;

	/** Constructs a new exchange connection with the given {@code HttpExchange}. */
	protected HttpExchangeConnection(HttpExchange httpExchange) {
		Assert.notNull(httpExchange, "'httpExchange' must not be null");
		this.httpExchange = httpExchange;
	}

	/** Returns the {@code HttpExchange} for this connection. */
	public HttpExchange getHttpExchange() {
		return this.httpExchange;
	}

	@Override
	public URI getUri() throws URISyntaxException {
		return this.httpExchange.getRequestURI();
	}

	void setChunkedEncoding(boolean chunkedEncoding) {
		this.chunkedEncoding = chunkedEncoding;
	}

	@Override
	public void endpointNotFound() {
		this.responseStatusCode = HttpTransportConstants.STATUS_NOT_FOUND;
	}

	/*
	 * Errors
	 */

	@Override
	public boolean hasError() throws IOException {
		return false;
	}

	@Override
	public @Nullable String getErrorMessage() throws IOException {
		return null;
	}

	/*
	 * Receiving request
	 */

	@Override
	public Iterator<String> getRequestHeaderNames() throws IOException {
		return this.httpExchange.getRequestHeaders().keySet().iterator();
	}

	@Override
	public Iterator<String> getRequestHeaders(String name) throws IOException {
		List<String> headers = this.httpExchange.getRequestHeaders().get(name);
		return (headers != null) ? headers.iterator() : Collections.emptyIterator();
	}

	@Override
	protected InputStream getRequestInputStream() throws IOException {
		return this.httpExchange.getRequestBody();
	}

	/*
	 * Sending response
	 */

	@Override
	public void addResponseHeader(String name, String value) throws IOException {
		this.httpExchange.getResponseHeaders().add(name, value);
	}

	@Override
	protected OutputStream getResponseOutputStream() throws IOException {
		if (this.chunkedEncoding) {
			this.httpExchange.sendResponseHeaders(this.responseStatusCode, 0);
			return this.httpExchange.getResponseBody();
		}
		else {
			if (this.responseBuffer == null) {
				this.responseBuffer = new ByteArrayOutputStream();
			}
			return this.responseBuffer;
		}
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		if (!this.chunkedEncoding) {
			Assert.state(this.responseBuffer != null, "responseBuffer has not been initialized");
			byte[] buf = this.responseBuffer.toByteArray();
			this.httpExchange.sendResponseHeaders(this.responseStatusCode, buf.length);
			OutputStream responseBody = this.httpExchange.getResponseBody();
			FileCopyUtils.copy(buf, responseBody);
		}
		this.responseBuffer = null;
	}

	@Override
	public void onClose() throws IOException {
		if (this.responseStatusCode == HttpTransportConstants.STATUS_ACCEPTED
				|| this.responseStatusCode == HttpTransportConstants.STATUS_NOT_FOUND) {
			this.httpExchange.sendResponseHeaders(this.responseStatusCode, -1);
		}
		this.httpExchange.close();
	}

	/*
	 * Faults
	 */

	@Override
	public boolean hasFault() throws IOException {
		return this.responseStatusCode == HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR;
	}

	@Override
	public void setFaultCode(@Nullable QName faultCode) throws IOException {
		if (faultCode != null) {
			if (SOAPConstants.SOAP_SENDER_FAULT.equals(faultCode)) {
				this.responseStatusCode = HttpTransportConstants.STATUS_BAD_REQUEST;
			}
			else {
				this.responseStatusCode = HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR;
			}
		}
		else {
			this.responseStatusCode = HttpTransportConstants.STATUS_OK;
		}
	}

}
