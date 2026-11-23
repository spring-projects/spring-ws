/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.util.Locale;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapMessage;

public class FaultEndpoint implements MessageEndpoint {

	@Override
	public void invoke(MessageContext messageContext) {

		SoapMessage response = (SoapMessage) messageContext.getResponse();
		response.getSoapBody().addServerOrReceiverFault("Something went wrong", Locale.ENGLISH);
	}

}
