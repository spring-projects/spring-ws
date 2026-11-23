/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.io.IOException;
import java.io.InputStream;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.MessageEOFException;

import org.springframework.util.Assert;

/**
 * Input stream that wraps a {@link BytesMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class BytesMessageInputStream extends InputStream {

	private final BytesMessage message;

	BytesMessageInputStream(BytesMessage message) {
		Assert.notNull(message, "'message' must not be null");
		this.message = message;
	}

	@Override
	public int read(byte[] b) throws IOException {
		try {
			return this.message.readBytes(b);
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (off == 0) {
			try {
				return this.message.readBytes(b, len);
			}
			catch (JMSException ex) {
				throw new JmsTransportException(ex);
			}
		}
		else {
			return super.read(b, off, len);
		}
	}

	@Override
	public int read() throws IOException {
		try {
			return this.message.readByte();
		}
		catch (MessageEOFException ex) {
			return -1;
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

}
