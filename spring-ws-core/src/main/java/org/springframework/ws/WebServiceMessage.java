/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

/**
 * Represents a protocol-agnostic XML message.
 * <p>
 * Contains methods that provide access to the payload of the message.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.soap.SoapMessage
 * @see WebServiceMessageFactory
 */
public interface WebServiceMessage {

	/**
	 * Returns the contents of the message as a {@link Source}.
	 * <p>
	 * Depending on the implementation, this can be retrieved multiple times, or just a
	 * single time.
	 * @return the message contents
	 */
	@Nullable Source getPayloadSource();

	/**
	 * Returns the contents of the message as a {@link Result}.
	 * <p>
	 * Calling this method removes the current payload.
	 * <p>
	 * Implementations that are read-only will throw an
	 * {@link UnsupportedOperationException}.
	 * @return the message contents
	 * @throws UnsupportedOperationException if the message is read-only
	 */
	Result getPayloadResult();

	/**
	 * Writes the entire message to the given output stream.
	 * <p>
	 * If the given stream is an instance of
	 * {@link org.springframework.ws.transport.TransportOutputStream}, the corresponding
	 * headers will be written as well.
	 * @param outputStream the stream to write to
	 * @throws IOException if an I/O exception occurs
	 */
	void writeTo(OutputStream outputStream) throws IOException;

}
