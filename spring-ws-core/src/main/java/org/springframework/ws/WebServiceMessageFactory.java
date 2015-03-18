/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws;

import java.io.IOException;
import java.io.InputStream;

/**
 * The {@code WebServiceMessageFactory} serves as a factory for {@link org.springframework.ws.WebServiceMessage
 * WebServiceMessages}.
 *
 * <p>Allows the creation of empty messages, or messages based on {@code InputStream}s.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.WebServiceMessage
 * @since 1.0.0
 */
public interface WebServiceMessageFactory {

	/**
	 * Creates a new, empty {@code WebServiceMessage}.
	 *
	 * @return the empty message
	 */
	WebServiceMessage createWebServiceMessage();

	/**
	 * Reads a {@link WebServiceMessage} from the given input stream.
	 *
	 * <p>If the given stream is an instance of {@link org.springframework.ws.transport.TransportInputStream
	 * TransportInputStream}, the headers will be read from the request.
	 *
	 * @param inputStream the input stream to read the message from
	 * @return the created message
	 * @throws InvalidXmlException if the XML read from the input stream is invalid
	 * @throws IOException if an I/O exception occurs
	 */
	WebServiceMessage createWebServiceMessage(InputStream inputStream) throws InvalidXmlException, IOException;

}
