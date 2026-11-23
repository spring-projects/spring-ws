/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jivesoftware.smack.packet.Message;

import org.springframework.util.Assert;

/**
 * Input stream that wraps a {@link Message}.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @since 2.0
 */
class MessageInputStream extends FilterInputStream {

	MessageInputStream(Message message, String encoding) throws IOException {
		super(createInputStream(message, encoding));
	}

	private static InputStream createInputStream(Message message, String encoding) throws IOException {
		Assert.notNull(message, "'message' must not be null");
		Assert.notNull(encoding, "'encoding' must not be null");
		String text = message.getBody();
		byte[] contents = (text != null) ? text.getBytes(encoding) : new byte[0];
		return new ByteArrayInputStream(contents);
	}

}
