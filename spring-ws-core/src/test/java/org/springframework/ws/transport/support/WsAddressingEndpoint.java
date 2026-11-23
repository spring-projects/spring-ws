/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.support;

import java.util.Locale;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.server.annotation.Action;

/**
 * Simple WS-Addressing endpoint used by integration tests.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
@Endpoint
public class WsAddressingEndpoint {

	public static final String REPLY_ACTION = "urn:reply";

	public static final String FAULT_ACTION = "urn:fault";

	@Action(REPLY_ACTION)
	public void reply(MessageContext messageContext) {
		messageContext.getResponse();
	}

	@Action(FAULT_ACTION)
	public void fault(MessageContext messageContext) {
		SoapMessage response = (SoapMessage) messageContext.getResponse();
		response.getSoapBody().addServerOrReceiverFault("error", Locale.ENGLISH);
	}

}
