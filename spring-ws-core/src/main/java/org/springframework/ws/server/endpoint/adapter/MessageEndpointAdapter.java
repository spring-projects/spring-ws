/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

/**
 * Adapter to use a {@code MessageEndpoint} as the endpoint for a
 * {@code EndpointInvocationChain}.
 * <p>
 * This adapter is registered by default by the {@link MessageDispatcher} and
 * {@link SoapMessageDispatcher}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.server.EndpointInvocationChain
 */
public class MessageEndpointAdapter implements EndpointAdapter {

	@Override
	public boolean supports(Object endpoint) {
		return endpoint instanceof MessageEndpoint;
	}

	@Override
	public void invoke(MessageContext messageContext, Object endpoint) throws Exception {
		((MessageEndpoint) endpoint).invoke(messageContext);
	}

}
