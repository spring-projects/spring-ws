/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * A {@code TransportOutputStream} is an output stream with MIME input headers. It is used
 * to write {@link org.springframework.ws.WebServiceMessage WebServiceMessages} to a
 * transport.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #addHeader(String,String)
 */
public abstract class TransportOutputStream extends OutputStream {

	private @Nullable OutputStream outputStream;

	protected TransportOutputStream() {
	}

	private OutputStream getOutputStream() throws IOException {
		if (this.outputStream == null) {
			this.outputStream = createOutputStream();
			Assert.notNull(this.outputStream, "outputStream must not be null");
		}
		return this.outputStream;
	}

	@Override
	public void close() throws IOException {
		if (this.outputStream != null) {
			getOutputStream().close();
		}
	}

	@Override
	public void flush() throws IOException {
		if (this.outputStream != null) {
			getOutputStream().flush();
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		getOutputStream().write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		getOutputStream().write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		getOutputStream().write(b);
	}

	/**
	 * Adds a header with the given name and value. This method can be called multiple
	 * times, to allow for headers with multiple values.
	 * @param name the name of the header
	 * @param value the value of the header
	 */
	public abstract void addHeader(String name, String value) throws IOException;

	/** Returns the output stream to write to. */
	protected abstract OutputStream createOutputStream() throws IOException;

}
