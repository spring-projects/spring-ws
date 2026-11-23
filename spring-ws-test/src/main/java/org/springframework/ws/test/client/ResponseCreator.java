/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Allows for creating up responses. Implementations of this interface are returned by
 * {@link ResponseCreators}.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface ResponseCreator {

	/**
	 * Create a response for the given the request and URI.
	 * @param uri the URI
	 * @param request the request message
	 * @param messageFactory the message that can be used to create responses
	 * @throws IOException in case of I/O errors
	 */
	WebServiceMessage createResponse(URI uri, WebServiceMessage request, WebServiceMessageFactory messageFactory)
			throws IOException;

}
