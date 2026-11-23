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
import org.jivesoftware.smack.packet.MessageBuilder;

import org.springframework.util.Assert;

/**
 * Output stream that updates a {@link MessageBuilder} when the body is flushed.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @since 2.0
 */
class MessageOutputStream extends FilterOutputStream {

	private final MessageBuilder messageBuilder;

	private final String encoding;

	MessageOutputStream(MessageBuilder messageBuilder, String encoding) {
		super(new ByteArrayOutputStream());
		Assert.notNull(messageBuilder, "'messageBuilder' must not be null");
		Assert.notNull(encoding, "'encoding' must not be null");
		this.messageBuilder = messageBuilder;
		this.encoding = encoding;
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		ByteArrayOutputStream bos = (ByteArrayOutputStream) this.out;
		String text = bos.toString(this.encoding);
		setBody(text);
	}

	/**
	 * Set the body of the message managed by this instance to the given {@code text}.
	 * <p>
	 * Remove the previous body, if any as {@link MessageBuilder} does not allow to mutate
	 * it.
	 * @param text the body
	 */
	private void setBody(String text) {
		for (Message.Body body : this.messageBuilder.getExtensions(Message.Body.class)) {
			this.messageBuilder.removeExtension(body);
		}
		this.messageBuilder.setBody(text);
	}

}
