/*
 * Copyright 2007 the original author or authors.
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
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Represents a point-to-point connection that a client can use for sending {@link WebServiceMessage} objects directly
 * to a remote party.
 * <p>
 * A {@code WebServiceConnection} can be obtained using a {@link WebServiceMessageSender}.
 *
 * @author Arjen Poutsma
 * @see WebServiceMessageSender#createConnection(URI)
 * @since 1.0.0
 */
public interface WebServiceConnection extends AutoCloseable {

	/**
	 * Sends the given message using this connection.
	 *
	 * @param message the message to be sent
	 * @throws IOException in case of I/O errors
	 */
	void send(WebServiceMessage message) throws IOException;

	/**
	 * Receives a message using the given {@link WebServiceMessageFactory}. This method blocks until it receives, or
	 * returns {@code null} when no message is received.
	 *
	 * @param messageFactory the message factory used for reading messages
	 * @return the read message, or {@code null} if no message received
	 * @throws IOException in case of I/O errors
	 */
	WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException;

	/** Returns the URI for this connection. */
	URI getUri() throws URISyntaxException;

	/**
	 * Indicates whether this connection has an error. Typically, error detection is done by inspecting connection error
	 * codes, etc.
	 *
	 * @return {@code true} if this connection has an error; {@code false} otherwise.
	 */
	boolean hasError() throws IOException;

	/**
	 * Returns the error message.
	 *
	 * @return the connection error message, if any; returns {@code null} when no error is present
	 * @see #hasError()
	 */
	String getErrorMessage() throws IOException;

	/**
	 * Closes this connection.
	 * <p>
	 * Once a connection has been closed, it is not available for further use. A new connection needs to be created.
	 *
	 * @throws IOException if an I/O error occurs when closing this connection
	 */
	void close() throws IOException;

}
