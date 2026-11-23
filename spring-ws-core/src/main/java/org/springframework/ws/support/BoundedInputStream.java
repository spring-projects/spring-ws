/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.support;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.util.unit.DataSize;

/**
 * An {@link InputStream} wrapper that limits the number of bytes read from an underlying
 * stream.
 * <p>
 * If the number of bytes read exceeds the specified max size, an {@link IOException} is
 * thrown.
 *
 * @author Stephane Nicoll
 * @since 3.1.10
 */
public class BoundedInputStream extends FilterInputStream {

	private final DataSize maxSize;

	private long bytesRead;

	/**
	 * Create a new instance.
	 * @param in the input stream to handle
	 * @param maxSize the maximum allowed size (must be positive)
	 */
	public BoundedInputStream(InputStream in, DataSize maxSize) {
		super(in);
		this.maxSize = maxSize;
		if (this.maxSize.toBytes() <= 0) {
			throw new IllegalArgumentException("maxSize must be greater than 0");
		}
	}

	public int read() throws IOException {
		int res = super.read();
		if (res != -1) {
			this.bytesRead++;
			checkLimit();
		}
		return res;
	}

	public int read(byte[] buffer, int off, int len) throws IOException {
		int res = super.read(buffer, off, len);
		if (res > 0) {
			this.bytesRead += res;
			checkLimit();
		}
		return res;
	}

	@Override
	public long skip(long n) throws IOException {
		long skipped = super.skip(n);
		if (skipped > 0) {
			this.bytesRead += skipped;
			checkLimit();
		}
		return skipped;
	}

	private void checkLimit() throws IOException {
		if (this.bytesRead > this.maxSize.toBytes()) {
			throw new IOException("Maximum message size exceeded: " + this.maxSize);
		}
	}

}
