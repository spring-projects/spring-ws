/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.transport;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the methods for classes capable of sending and receiving {@link WebServiceMessage} instances across a
 * transport.
 * <p>
 * The {@code WebServiceMessageSender} is basically a factory for {@link WebServiceConnection} objects.
 *
 * @author Arjen Poutsma
 * @see WebServiceConnection
 * @since 1.0.0
 */
public interface WebServiceMessageSender {

	/**
	 * Create a new {@link WebServiceConnection} to the specified URI.
	 *
	 * @param uri the URI to open a connection to
	 * @return the new connection
	 * @throws IOException in case of I/O errors
	 */
	WebServiceConnection createConnection(URI uri) throws IOException;

	/**
	 * Does this {@link WebServiceMessageSender} support the supplied URI?
	 *
	 * @param uri the URI to be checked
	 * @return {@code true} if this {@code WebServiceMessageSender} supports the supplied URI
	 */
	boolean supports(URI uri);

}
