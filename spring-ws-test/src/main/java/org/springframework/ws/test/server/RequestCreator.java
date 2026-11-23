/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Creates request messages. Implementations of this interface are returned by
 * {@link RequestCreators}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 * @see RequestCreators
 */
public interface RequestCreator {

	/**
	 * Create a request.
	 * @param messageFactory the message that can be used to create responses
	 * @throws IOException in case of I/O errors
	 */
	WebServiceMessage createRequest(WebServiceMessageFactory messageFactory) throws IOException;

}
