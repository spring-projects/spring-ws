/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
