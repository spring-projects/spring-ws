/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

/**
 * @author Arjen Poutsma
 */
public class ResponseEndpoint implements MessageEndpoint {

	@Override
	public void invoke(MessageContext messageContext) {
		messageContext.getResponse();
	}

}
