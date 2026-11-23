/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import org.jivesoftware.smack.packet.Message;

import org.springframework.util.Assert;

/**
 * Output stream that wraps a {@link Message}.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @since 2.0
 */
class MessageOutputStream extends FilterOutputStream {

	private final Message message;

	private final String encoding;

	MessageOutputStream(Message message, String encoding) {
		super(new ByteArrayOutputStream());
		Assert.notNull(message, "'message' must not be null");
		Assert.notNull(encoding, "'encoding' must not be null");
		this.message = message;
		this.encoding = encoding;
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		ByteArrayOutputStream bos = (ByteArrayOutputStream) this.out;
		String text = bos.toString(this.encoding);
		this.message.setBody(text);
	}

}
