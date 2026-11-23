/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.jspecify.annotations.Nullable;

/**
 * Callback interface for extracting a result object from a
 * {@link javax.xml.transform.Source} instance.
 * <p>
 * Used for output object creation in {@link WebServiceTemplate}. Alternatively, output
 * sources can also be returned to client code as-is. In case of a source as execution
 * result, you will almost always want to implement a {@code SourceExtractor}, to be able
 * to read the message content in a managed fashion, with the connection still open while
 * reading the message.
 * <p>
 * Implementations of this interface perform the actual work of extracting results, but
 * don't need to worry about exception handling, or resource handling.
 *
 * @param <T> the type of the source
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.client.core.WebServiceTemplate
 */
public interface SourceExtractor<T> {

	/**
	 * Process the data in the given {@code Source}, creating a corresponding result
	 * object.
	 * @param source the message payload to extract data from
	 * @return an arbitrary result object, or {@code null} if none (the extractor will
	 * typically be stateful in the latter case)
	 * @throws IOException in case of I/O errors
	 */
	@Nullable T extractData(@Nullable Source source) throws IOException, TransformerException;

}
