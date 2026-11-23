/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;

import org.springframework.util.Assert;

/**
 * Input stream that wraps a {@link TextMessage}.
 *
 * @author Arjen Poutsma
 * @since 1.5.3
 */
class TextMessageInputStream extends FilterInputStream {

	TextMessageInputStream(TextMessage message, String encoding) throws IOException {
		super(createInputStream(message, encoding));
	}

	private static InputStream createInputStream(TextMessage message, String encoding) throws IOException {
		Assert.notNull(message, "'message' must not be null");
		Assert.notNull(encoding, "'encoding' must not be null");
		try {
			String text = message.getText();
			byte[] contents = (text != null) ? text.getBytes(encoding) : new byte[0];
			return new ByteArrayInputStream(contents);
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

}
