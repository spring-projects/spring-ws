/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.creator;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * Defines the general contract for creating messages used in test scenarios.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface WebServiceMessageCreator {

	/**
	 * Create a message.
	 * @param messageFactory the message that can be used to create the message
	 * @throws IOException in case of I/O errors
	 */
	WebServiceMessage createMessage(WebServiceMessageFactory messageFactory) throws IOException;

}
