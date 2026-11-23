/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.ws.WebServiceMessage;

/**
 * Generic callback interface for code that operates on a {@link WebServiceMessage}.
 * <p>
 * Implementations can execute any number of operations on the message, such as set the
 * contents of the message, or set the {@code SOAPAction} header.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WebServiceMessageCallback {

	/**
	 * Execute any number of operations on the supplied {@code message}.
	 * @param message the message
	 * @throws IOException in case of I/O errors
	 * @throws TransformerException in case of transformation errors
	 */
	void doWithMessage(WebServiceMessage message) throws IOException, TransformerException;

}
