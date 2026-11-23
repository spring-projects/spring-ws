/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

import org.springframework.util.Assert;

/**
 * Writer that wraps a {@link TextMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.5.3
 */
class TextMessageOutputStream extends FilterOutputStream {

	private final TextMessage message;

	private final String encoding;

	TextMessageOutputStream(TextMessage message, String encoding) {
		super(new ByteArrayOutputStream());
		Assert.notNull(message, "'message' must not be null");
		Assert.notNull(encoding, "'encoding' must not be null");
		this.message = message;
		this.encoding = encoding;
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		try {
			ByteArrayOutputStream baos = (ByteArrayOutputStream) this.out;
			String text = baos.toString(this.encoding);
			this.message.setText(text);
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

}
