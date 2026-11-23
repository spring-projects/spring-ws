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
 * Abstract base class for the {@link WebServiceMessageCreator} interface.
 * <p>
 * Creates a message using the given {@link WebServiceMessageFactory}, and passes it on to
 * {@link #doWithMessage(WebServiceMessage)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public abstract class AbstractMessageCreator implements WebServiceMessageCreator {

	@Override
	public final WebServiceMessage createMessage(WebServiceMessageFactory messageFactory) throws IOException {
		WebServiceMessage message = messageFactory.createWebServiceMessage();
		doWithMessage(message);
		return message;
	}

	/**
	 * Abstract template method, invoked by
	 * {@link #createMessage(WebServiceMessageFactory)} after a message has been created.
	 * @param message the message
	 * @throws IOException in case of I/O errors
	 */
	protected abstract void doWithMessage(WebServiceMessage message) throws IOException;

}
