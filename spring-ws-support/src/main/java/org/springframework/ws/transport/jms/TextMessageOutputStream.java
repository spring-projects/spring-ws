/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.springframework.util.Assert;

/**
 * Writer that wraps a {@link javax.jms.TextMessage}.
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
			ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
			String text = new String(baos.toByteArray(), encoding);
			message.setText(text);
		} catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}
}
