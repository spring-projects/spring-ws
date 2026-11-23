/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import org.springframework.ws.context.MessageContext;

/**
 * Defines the methods for classes capable of receiving
 * {@link org.springframework.ws.WebServiceMessage} instances coming in on a transport.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface WebServiceMessageReceiver {

	/**
	 * Receives the given message context. The given message context can be used to create
	 * a response.
	 * @param messageContext the message context to be received
	 */
	void receive(MessageContext messageContext) throws Exception;

}
