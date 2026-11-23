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
 * Abstract base class for {@link WebServiceConnection} implementations used for receiving
 * requests.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.0.0
 */
public abstract class AbstractReceiverConnection extends AbstractWebServiceConnection
		implements HeadersAwareReceiverWebServiceConnection {

	private @Nullable TransportInputStream requestInputStream;

	private @Nullable TransportOutputStream responseOutputStream;

	@Override
	protected final TransportInputStream createTransportInputStream() throws IOException {
		if (this.requestInputStream == null) {
			this.requestInputStream = new RequestTransportInputStream();
		}
		return this.requestInputStream;
	}

	@Override
	protected final TransportOutputStream createTransportOutputStream() throws IOException {
		if (this.responseOutputStream == null) {
			this.responseOutputStream = new ResponseTransportOutputStream();
		}
		return this.responseOutputStream;
	}

	/**
	 * Template method invoked from {@link #close()}. Default implementation is empty.
	 * @throws IOException if an I/O error occurs when closing this connection
	 */
	@Override
	protected void onClose() throws IOException {
	}

	/** Returns the input stream to read the response from. */
	protected abstract InputStream getRequestInputStream() throws IOException;

	/** Returns the output stream to write the request to. */
	protected abstract OutputStream getResponseOutputStream() throws IOException;

	/** Implementation of {@code TransportInputStream} for receiving-side connections. */
	private final class RequestTransportInputStream extends TransportInputStream {

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
	private final class ResponseTransportOutputStream extends TransportOutputStream {

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
