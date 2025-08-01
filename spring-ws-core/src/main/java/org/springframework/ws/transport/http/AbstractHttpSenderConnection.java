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
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Abstract base class for {@link WebServiceConnection} implementations that send request
 * over HTTP.
 *
 * @author Arjen Poutsma
 * @author Andreas Veithen
 * @since 1.0.0
 */
public abstract class AbstractHttpSenderConnection extends AbstractSenderConnection
		implements FaultAwareWebServiceConnection {

	/**
	 * Cached result of {@code hasResponse}.
	 */
	private @Nullable Boolean hasResponse;

	/**
	 * The raw response input stream to use instead of calling
	 * {@link #getRawResponseInputStream()}.
	 */
	private @Nullable PushbackInputStream rawResponseInputStream;

	@Override
	public final boolean hasError() throws IOException {
		return getResponseCode() / 100 != 2;
	}

	@Override
	public final String getErrorMessage() throws IOException {
		StringBuilder builder = new StringBuilder();
		String responseMessage = getResponseMessage();
		if (StringUtils.hasLength(responseMessage)) {
			builder.append(responseMessage);
		}
		builder.append(" [");
		builder.append(getResponseCode());
		builder.append(']');
		return builder.toString();
	}

	/*
	 * Receiving response
	 */
	@Override
	protected final boolean hasResponse() throws IOException {
		int responseCode = getResponseCode();
		if (HttpTransportConstants.STATUS_ACCEPTED == responseCode
				|| HttpTransportConstants.STATUS_NO_CONTENT == responseCode) {
			return false;
		}
		if (this.hasResponse != null) {
			return this.hasResponse;
		}
		long contentLength = getResponseContentLength();
		if (contentLength < 0) {
			this.rawResponseInputStream = new PushbackInputStream(getRawResponseInputStream());
			int b = this.rawResponseInputStream.read();
			if (b == -1) {
				this.hasResponse = Boolean.FALSE;
			}
			else {
				this.hasResponse = Boolean.TRUE;
				this.rawResponseInputStream.unread(b);
			}
		}
		else {
			this.hasResponse = contentLength > 0;
		}
		return this.hasResponse;
	}

	@Override
	protected final InputStream getResponseInputStream() throws IOException {
		InputStream inputStream = this.rawResponseInputStream;
		if (inputStream == null) {
			inputStream = getRawResponseInputStream();
		}
		return (isGzipResponse()) ? new GZIPInputStream(inputStream) : inputStream;
	}

	/** Determine whether the response is a GZIP response. */
	private boolean isGzipResponse() throws IOException {
		Iterator<String> iterator = getResponseHeaders(HttpTransportConstants.HEADER_CONTENT_ENCODING);
		if (iterator.hasNext()) {
			String encodingHeader = iterator.next();
			return encodingHeader.toLowerCase().contains(HttpTransportConstants.CONTENT_ENCODING_GZIP);
		}
		return false;
	}

	/** Returns the HTTP status code of the response. */
	protected abstract int getResponseCode() throws IOException;

	/** Returns the HTTP status message of the response. */
	protected abstract String getResponseMessage() throws IOException;

	/** Returns the length of the response. */
	protected abstract long getResponseContentLength() throws IOException;

	/** Returns the raw, possibly compressed input stream to read the response from. */
	protected abstract InputStream getRawResponseInputStream() throws IOException;

	/*
	 * Faults
	 */

	@Override
	public final boolean hasFault() throws IOException {
		// SOAP 1.1 specifies a 500 status code for faults
		// SOAP 1.2 specifies a 400 status code for sender faults, and 500 for all other
		// faults
		return switch (getResponseCode()) {
			case HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR -> isSoap11Response() || isSoap12Response();
			case HttpTransportConstants.STATUS_BAD_REQUEST -> isSoap12Response();
			default -> false;
		};
	}

	/** Determine whether the response is a SOAP 1.1 message. */
	private boolean isSoap11Response() throws IOException {
		Iterator<String> iterator = getResponseHeaders(HttpTransportConstants.HEADER_CONTENT_TYPE);
		if (iterator.hasNext()) {
			String contentType = iterator.next().toLowerCase();
			return contentType.contains("text/xml");
		}
		return false;
	}

	/** Determine whether the response is a SOAP 1.1 message. */
	private boolean isSoap12Response() throws IOException {
		Iterator<String> iterator = getResponseHeaders(HttpTransportConstants.HEADER_CONTENT_TYPE);
		if (iterator.hasNext()) {
			String contentType = iterator.next().toLowerCase();
			return contentType.contains("application/soap+xml");
		}
		return false;
	}

	@Override
	public final void setFaultCode(@Nullable QName faultCode) throws IOException {
	}

}
