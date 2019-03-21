/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.util.Assert;

import org.jivesoftware.smack.packet.Message;

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
		ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
		String text = new String(bos.toByteArray(), encoding);
		message.setBody(text);
	}
}