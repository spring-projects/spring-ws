/*
 * Copyright 2005-2022 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.springframework.util.Assert;

/**
 * Input stream that wraps a {@link javax.jms.TextMessage}.
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
			byte[] contents = text != null ? text.getBytes(encoding) : new byte[0];
			return new ByteArrayInputStream(contents);
		} catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}
}
