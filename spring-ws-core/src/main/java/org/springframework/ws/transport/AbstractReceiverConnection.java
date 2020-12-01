/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Abstract base class for {@link WebServiceConnection} implementations used for receiving requests.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 */
public abstract class AbstractReceiverConnection extends AbstractWebServiceConnection
		implements HeadersAwareReceiverWebServiceConnection {

	private TransportInputStream requestInputStream;

	private TransportOutputStream responseOutputStream;

	@Override
	protected final TransportInputStream createTransportInputStream() throws IOException {
		if (requestInputStream == null) {
			requestInputStream = new RequestTransportInputStream();
		}
		return requestInputStream;
	}

	@Override
	protected final TransportOutputStream createTransportOutputStream() throws IOException {
		if (responseOutputStream == null) {
			responseOutputStream = new ResponseTransportOutputStream();
		}
		return responseOutputStream;
	}

	/**
	 * Template method invoked from {@link #close()}. Default implementation is empty.
	 *
	 * @throws IOException if an I/O error occurs when closing this connection
	 */
	@Override
	protected void onClose() throws IOException {}

	/** Returns the input stream to read the response from. */
	protected abstract InputStream getRequestInputStream() throws IOException;

	/** Returns the output stream to write the request to. */
	protected abstract OutputStream getResponseOutputStream() throws IOException;

	/** Implementation of {@code TransportInputStream} for receiving-side connections. */
	private class RequestTransportInputStream extends TransportInputStream {

		@Override
		protected InputStream createInputStream() throws IOException {
			return getRequestInputStream();
		}

		@Override
		public Iterator<String> getHeaderNames() throws IOException {
			return getRequestHeaderNames();
		}

		@Override
		public Iterator<String> getHeaders(String name) throws IOException {
			return getRequestHeaders(name);
		}

	}

	/** Implementation of {@code TransportOutputStream} for sending-side connections. */
	private class ResponseTransportOutputStream extends TransportOutputStream {

		@Override
		public void addHeader(String name, String value) throws IOException {
			addResponseHeader(name, value);
		}

		@Override
		protected OutputStream createOutputStream() throws IOException {
			return getResponseOutputStream();
		}

	}

}
