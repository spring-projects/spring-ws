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

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

import java.io.IOException;
import java.io.OutputStream;

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
	public void write(byte b[]) throws IOException {
		try {
			message.writeBytes(b);
		} catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		try {
			message.writeBytes(b, off, len);
		} catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}

	@Override
	public void write(int b) throws IOException {
		try {
			message.writeByte((byte) b);
		} catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
	}
}
