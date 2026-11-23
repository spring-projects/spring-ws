/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Represents a point-to-point connection that a client can use for sending
 * {@link WebServiceMessage} objects directly to a remote party.
 * <p>
 * A {@code WebServiceConnection} can be obtained using a {@link WebServiceMessageSender}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see WebServiceMessageSender#createConnection(URI)
 */
public interface WebServiceConnection extends AutoCloseable {

	/**
	 * Sends the given message using this connection.
	 * @param message the message to be sent
	 * @throws IOException in case of I/O errors
	 */
	void send(WebServiceMessage message) throws IOException;

	/**
	 * Receives a message using the given {@link WebServiceMessageFactory}. This method
	 * blocks until it receives, or returns {@code null} when no message is received.
	 * @param messageFactory the message factory used for reading messages
	 * @return the read message, or {@code null} if no message received
	 * @throws IOException in case of I/O errors
	 */
	@Nullable WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException;

	/** Returns the URI for this connection. */
	@Nullable URI getUri() throws URISyntaxException;

	/**
	 * Indicates whether this connection has an error. Typically, error detection is done
	 * by inspecting connection error codes, etc.
	 * @return {@code true} if this connection has an error; {@code false} otherwise.
	 */
	boolean hasError() throws IOException;

	/**
	 * Returns the error message.
	 * @return the connection error message, if any; returns {@code null} when no error is
	 * present
	 * @see #hasError()
	 */
	@Nullable String getErrorMessage() throws IOException;

	/**
	 * Closes this connection.
	 * <p>
	 * Once a connection has been closed, it is not available for further use. A new
	 * connection needs to be created.
	 * @throws IOException if an I/O error occurs when closing this connection
	 */
	void close() throws IOException;

}
