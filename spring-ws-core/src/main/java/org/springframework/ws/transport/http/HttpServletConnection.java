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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.xml.namespace.QName;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.SOAPConstants;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.EndpointAwareWebServiceConnection;
import org.springframework.ws.transport.FaultAwareWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * Implementation of {@link WebServiceConnection} that is based on the Servlet API.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 */
public class HttpServletConnection extends AbstractReceiverConnection
		implements EndpointAwareWebServiceConnection, FaultAwareWebServiceConnection {

	private final HttpServletRequest httpServletRequest;

	private final HttpServletResponse httpServletResponse;

	private boolean statusCodeSet = false;

	/**
	 * Constructs a new servlet connection with the given {@code HttpServletRequest} and
	 * {@code HttpServletResponse}.
	 */
	protected HttpServletConnection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		this.httpServletRequest = httpServletRequest;
		this.httpServletResponse = httpServletResponse;
	}

	/** Returns the {@code HttpServletRequest} for this connection. */
	public HttpServletRequest getHttpServletRequest() {
		return this.httpServletRequest;
	}

	/** Returns the {@code HttpServletResponse} for this connection. */
	public HttpServletResponse getHttpServletResponse() {
		return this.httpServletResponse;
	}

	@Override
	public void endpointNotFound() {
		getHttpServletResponse().setStatus(HttpTransportConstants.STATUS_NOT_FOUND);
		this.statusCodeSet = true;
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
	 * URI
	 */

	@Override
	public URI getUri() throws URISyntaxException {
		return new URI(this.httpServletRequest.getScheme(), null, this.httpServletRequest.getServerName(),
				this.httpServletRequest.getServerPort(), this.httpServletRequest.getRequestURI(),
				this.httpServletRequest.getQueryString(), null);
	}

	/*
	 * Receiving request
	 */

	@Override
	public Iterator<String> getRequestHeaderNames() throws IOException {
		return new EnumerationIterator<>(getHttpServletRequest().getHeaderNames());
	}

	@Override
	public Iterator<String> getRequestHeaders(String name) throws IOException {
		return new EnumerationIterator<>(getHttpServletRequest().getHeaders(name));
	}

	@Override
	protected InputStream getRequestInputStream() throws IOException {
		return getHttpServletRequest().getInputStream();
	}

	/*
	 * Sending response
	 */

	@Override
	public void addResponseHeader(String name, String value) throws IOException {
		getHttpServletResponse().addHeader(name, value);
	}

	@Override
	protected OutputStream getResponseOutputStream() throws IOException {
		return getHttpServletResponse().getOutputStream();
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		this.statusCodeSet = true;
	}

	@Override
	public void onClose() throws IOException {
		if (!this.statusCodeSet) {
			getHttpServletResponse().setStatus(HttpTransportConstants.STATUS_ACCEPTED);
		}
	}

	/*
	 * Faults
	 */

	@Override
	public boolean hasFault() throws IOException {
		return false;
	}

	@Override
	public void setFaultCode(@Nullable QName faultCode) throws IOException {
		if (faultCode != null) {
			if (SOAPConstants.SOAP_SENDER_FAULT.equals(faultCode)) {
				getHttpServletResponse().setStatus(HttpTransportConstants.STATUS_BAD_REQUEST);
			}
			else {
				getHttpServletResponse().setStatus(HttpTransportConstants.STATUS_INTERNAL_SERVER_ERROR);
			}
		}
		else {
			getHttpServletResponse().setStatus(HttpTransportConstants.STATUS_OK);
		}
		this.statusCodeSet = true;
	}

}
