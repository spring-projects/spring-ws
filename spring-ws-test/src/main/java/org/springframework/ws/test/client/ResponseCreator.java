/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Allows for creating up responses. Implementations of this interface are returned by {@link ResponseCreators}.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface ResponseCreator {

	/**
	 * Create a response for the given the request and URI.
	 *
	 * @param uri the URI
	 * @param request the request message
	 * @param messageFactory the message that can be used to create responses
	 * @throws IOException in case of I/O errors
	 */
	WebServiceMessage createResponse(URI uri, WebServiceMessage request, WebServiceMessageFactory messageFactory)
			throws IOException;

}
