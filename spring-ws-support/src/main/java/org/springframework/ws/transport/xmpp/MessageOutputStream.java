/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
