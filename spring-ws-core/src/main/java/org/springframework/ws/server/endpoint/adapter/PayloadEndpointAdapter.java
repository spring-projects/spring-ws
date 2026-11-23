/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.adapter;

import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.PayloadEndpoint;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.xml.transform.TransformerObjectSupport;

/**
 * Adapter to use a {@code PayloadEndpoint} as the endpoint for a
 * {@code EndpointInvocationChain}.
 * <p>
 * This adapter is registered by default by the {@link MessageDispatcher} and
 * {@link SoapMessageDispatcher}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.server.endpoint.PayloadEndpoint
 * @see org.springframework.ws.server.EndpointInvocationChain
 */
public class PayloadEndpointAdapter extends TransformerObjectSupport implements EndpointAdapter {

	@Override
	public boolean supports(Object endpoint) {
		return endpoint instanceof PayloadEndpoint;
	}

	@Override
	public void invoke(MessageContext messageContext, Object endpoint) throws Exception {
		PayloadEndpoint payloadEndpoint = (PayloadEndpoint) endpoint;
		Source requestSource = messageContext.getRequest().getPayloadSource();
		Source responseSource = payloadEndpoint.invoke(requestSource);
		if (responseSource != null) {
			WebServiceMessage response = messageContext.getResponse();
			transform(responseSource, response.getPayloadResult());
		}
	}

}
