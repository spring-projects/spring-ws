/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceMessage;

/**
 * Callback interface for extracting a result object from a {@link WebServiceMessage}
 * instance.
 * <p>
 * Used for output object creation in {@link WebServiceTemplate}. Alternatively, output
 * messages can also be returned to client code as-is. In case of a message as execution
 * result, you will almost always want to implement a {@code WebServiceMessageExtractor},
 * to be able to read the message in a managed fashion, with the connection still open
 * while reading the message.
 * <p>
 * Implementations of this interface perform the actual work of extracting results, but
 * don't need to worry about exception handling, or resource handling.
 *
 * @param <T> the type of the result object
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WebServiceMessageExtractor<T> {

	/**
	 * Process the data in the given {@code WebServiceMessage}, creating a corresponding
	 * result object.
	 * @param message the message to extract data from (possibly a {@code SoapMessage})
	 * @return an arbitrary result object, or {@code null} if none (the extractor will
	 * typically be stateful in the latter case)
	 * @throws IOException in case of I/O errors
	 * @throws TransformerException in case of transformation errors
	 */
	@Nullable T extractData(WebServiceMessage message) throws IOException, TransformerException;

}
