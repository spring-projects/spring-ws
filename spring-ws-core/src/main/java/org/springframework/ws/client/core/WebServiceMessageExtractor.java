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

package org.springframework.ws.client.core;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.springframework.ws.WebServiceMessage;

/**
 * Callback interface for extracting a result object from a {@link WebServiceMessage} instance.
 * <p>
 * Used for output object creation in {@link WebServiceTemplate}. Alternatively, output messages can also be returned to
 * client code as-is. In case of a message as execution result, you will almost always want to implement a
 * {@code WebServiceMessageExtractor}, to be able to read the message in a managed fashion, with the connection still
 * open while reading the message.
 * <p>
 * Implementations of this interface perform the actual work of extracting results, but don't need to worry about
 * exception handling, or resource handling.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WebServiceMessageExtractor<T> {

	/**
	 * Process the data in the given {@code WebServiceMessage}, creating a corresponding result object.
	 *
	 * @param message the message to extract data from (possibly a {@code SoapMessage})
	 * @return an arbitrary result object, or {@code null} if none (the extractor will typically be stateful in the latter
	 *         case)
	 * @throws IOException in case of I/O errors
	 * @throws TransformerException in case of transformation errors
	 */
	T extractData(WebServiceMessage message) throws IOException, TransformerException;

}
