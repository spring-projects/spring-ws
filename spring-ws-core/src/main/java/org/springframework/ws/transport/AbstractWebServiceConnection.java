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

package org.springframework.ws.transport;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Abstract base class for {@link WebServiceConnection} implementations.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractWebServiceConnection implements WebServiceConnection {

	private @Nullable TransportInputStream tis;

	private @Nullable TransportOutputStream tos;

	private boolean closed = false;

	@Override
	public final void send(WebServiceMessage message) throws IOException {
		checkClosed();
		onSendBeforeWrite(message);
		this.tos = createTransportOutputStream();
		if (this.tos == null) {
			return;
		}
		message.writeTo(this.tos);
		this.tos.flush();
		onSendAfterWrite(message);
	}

	/**
	 * Called before the given message has been written to the
	 * {@code TransportOutputStream}. Called from {@link #send(WebServiceMessage)}.
	 * <p>
	 * Default implementation does nothing.
	 * @param message the message
	 * @throws IOException when an I/O exception occurs
	 */
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
	}

	/**
	 * Returns a {@code TransportOutputStream} for the given message. Called from
	 * {@link #send(WebServiceMessage)}.
	 * @return the output stream
	 * @throws IOException when an I/O exception occurs
	 */
	protected abstract @Nullable TransportOutputStream createTransportOutputStream() throws IOException;

	/**
	 * Called after the given message has been written to the
	 * {@code TransportOutputStream}. Called from {@link #send(WebServiceMessage)}.
	 * <p>
	 * Default implementation does nothing.
	 * @param message the message
	 * @throws IOException when an I/O exception occurs
	 */
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
	}

	@Override
	public final @Nullable WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException {
		checkClosed();
		onReceiveBeforeRead();
		this.tis = createTransportInputStream();
		if (this.tis == null) {
			return null;
		}
		WebServiceMessage message = messageFactory.createWebServiceMessage(this.tis);
		onReceiveAfterRead(message);
		return message;
	}

	/**
	 * Called before a message has been read from the {@code TransportInputStream}. Called
	 * from {@link #receive(WebServiceMessageFactory)}.
	 * <p>
	 * Default implementation does nothing.
	 * @throws IOException when an I/O exception occurs
	 */
	protected void onReceiveBeforeRead() throws IOException {
	}

	/**
	 * Returns a {@code TransportInputStream}. Called from
	 * {@link #receive(WebServiceMessageFactory)}.
	 * @return the input stream, or {@code null} if no response can be read
	 * @throws IOException when an I/O exception occurs
	 */
	protected abstract @Nullable TransportInputStream createTransportInputStream() throws IOException;

	/**
	 * Called when the given message has been read from the {@code TransportInputStream}.
	 * Called from {@link #receive(WebServiceMessageFactory)}.
	 * <p>
	 * Default implementation does nothing.
	 * @param message the message
	 * @throws IOException when an I/O exception occurs
	 */
	protected void onReceiveAfterRead(WebServiceMessage message) throws IOException {
	}

	@Override
	public final void close() throws IOException {
		IOException ioex = null;
		if (this.tis != null) {
			try {
				this.tis.close();
			}
			catch (IOException ex) {
				ioex = ex;
			}
		}
		if (this.tos != null) {
			try {
				this.tos.close();
			}
			catch (IOException ex) {
				ioex = ex;
			}
		}
		onClose();
		this.closed = true;
		if (ioex != null) {
			throw ioex;
		}
	}

	private void checkClosed() {
		if (this.closed) {
			throw new IllegalStateException("Connection has been closed and cannot be reused.");
		}
	}

	/**
	 * Template method invoked from {@link #close()}. Default implementation is empty.
	 * @throws IOException if an I/O error occurs when closing this connection
	 */
	protected void onClose() throws IOException {
	}

}
