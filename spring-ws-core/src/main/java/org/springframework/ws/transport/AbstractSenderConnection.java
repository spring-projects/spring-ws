/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for {@link WebServiceConnection} implementations used for sending
 * requests.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 */
public abstract class AbstractSenderConnection extends AbstractWebServiceConnection
		implements HeadersAwareSenderWebServiceConnection {

	private @Nullable TransportOutputStream requestOutputStream;

	private @Nullable TransportInputStream responseInputStream;

	@Override
	protected final TransportOutputStream createTransportOutputStream() throws IOException {
		if (this.requestOutputStream == null) {
			this.requestOutputStream = new RequestTransportOutputStream();
		}
		return this.requestOutputStream;
	}

	@Override
	protected final @Nullable TransportInputStream createTransportInputStream() throws IOException {
		if (hasResponse()) {
			if (this.responseInputStream == null) {
				this.responseInputStream = new ResponseTransportInputStream();
			}
			return this.responseInputStream;
		}
		else {
			return null;
		}
	}

	/**
	 * Template method invoked from {@link #close()}. Default implementation is empty.
	 * @throws IOException if an I/O error occurs when closing this connection
	 */
	@Override
	protected void onClose() throws IOException {
	}

	/** Indicates whether this connection has a response. */
	protected abstract boolean hasResponse() throws IOException;

	/** Returns the output stream to write the request to. */
	protected abstract OutputStream getRequestOutputStream() throws IOException;

	/** Returns the input stream to read the response from. */
	protected abstract InputStream getResponseInputStream() throws IOException;

	/** Implementation of {@code TransportInputStream} for receiving-side connections. */
	class RequestTransportOutputStream extends TransportOutputStream {

		@Override
		public void addHeader(String name, String value) throws IOException {
			addRequestHeader(name, value);
		}

		@Override
		protected OutputStream createOutputStream() throws IOException {
			return getRequestOutputStream();
		}

	}

	/** Implementation of {@link TransportInputStream} for client-side HTTP. */
	class ResponseTransportInputStream extends TransportInputStream {

		@Override
		protected InputStream createInputStream() throws IOException {
			return getResponseInputStream();
		}

		@Override
		public Iterator<String> getHeaderNames() throws IOException {
			return getResponseHeaderNames();
		}

		@Override
		public Iterator<String> getHeaders(String name) throws IOException {
			return getResponseHeaders(name);
		}

	}

}
