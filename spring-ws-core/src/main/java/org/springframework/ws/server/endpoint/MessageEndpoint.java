/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import org.springframework.ws.context.MessageContext;

/**
 * Defines the basic contract for Web Services interested in the entire message payload.
 * <p>
 * The main entrypoint is {@link #invoke(MessageContext)}, which gets invoked with the
 * message context. This context contains the {@link MessageContext#getRequest() request},
 * and can be used to create a response.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.server.endpoint.PayloadEndpoint
 */
public interface MessageEndpoint {

	/**
	 * Invokes an operation.
	 * <p>
	 * The given {@code messageContext} can be used to create a response.
	 * @param messageContext the message context
	 * @throws Exception if an exception occurs
	 */
	void invoke(MessageContext messageContext) throws Exception;

}
