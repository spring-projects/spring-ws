/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

import org.springframework.util.Assert;

/**
 * Output stream that wraps a {@link BytesMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class BytesMessageOutputStream extends OutputStream {

	private final BytesMessage message;

	BytesMessageOutputStream(BytesMessage message) {
		Assert.notNull(message, "'message' must not be null");
		this.message = message;
	}

	@Override
	public void write(byte[] b) throws IOException {
		try {
			this.message.writeBytes(b);
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try {
			this.message.writeBytes(b, off, len);
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

	@Override
	public void write(int b) throws IOException {
		try {
			this.message.writeByte((byte) b);
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

}
