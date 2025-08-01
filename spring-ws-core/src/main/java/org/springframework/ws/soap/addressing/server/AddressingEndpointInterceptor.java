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

package org.springframework.ws.soap.addressing.server;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * {@link SoapEndpointInterceptor} implementation that deals with WS-Addressing headers.
 * Stateful, and instantiated by the {@link AbstractAddressingEndpointMapping}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
class AddressingEndpointInterceptor implements SoapEndpointInterceptor {

	private static final Log logger = LogFactory.getLog(AddressingEndpointInterceptor.class);

	private final AddressingVersion version;

	private final MessageIdStrategy messageIdStrategy;

	private final WebServiceMessageSender[] messageSenders;

	private final @Nullable URI replyAction;

	private final @Nullable URI faultAction;

	AddressingEndpointInterceptor(AddressingVersion version, MessageIdStrategy messageIdStrategy,
			WebServiceMessageSender[] messageSenders, @Nullable URI replyAction, @Nullable URI faultAction) {
		Assert.notNull(version, "version must not be null");
		Assert.notNull(messageIdStrategy, "messageIdStrategy must not be null");
		Assert.notNull(messageSenders, "'messageSenders' must not be null");
		this.version = version;
		this.messageIdStrategy = messageIdStrategy;
		this.messageSenders = messageSenders;
		this.replyAction = replyAction;
		this.faultAction = faultAction;
	}

	@Override
	public final boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
		SoapMessage request = (SoapMessage) messageContext.getRequest();
		MessageAddressingProperties requestMap = this.version.getMessageAddressingProperties(request);
		if (!this.version.hasRequiredProperties(requestMap)) {
			this.version.addMessageAddressingHeaderRequiredFault((SoapMessage) messageContext.getResponse());
			return false;
		}
		if (this.messageIdStrategy.isDuplicate(requestMap.getMessageId())) {
			this.version.addInvalidAddressingHeaderFault((SoapMessage) messageContext.getResponse());
			return false;
		}
		return true;
	}

	@Override
	public final boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return handleResponseOrFault(messageContext, false);
	}

	@Override
	public final boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return handleResponseOrFault(messageContext, true);
	}

	private boolean handleResponseOrFault(MessageContext messageContext, boolean isFault) throws Exception {
		Assert.isInstanceOf(SoapMessage.class, messageContext.getRequest());
		Assert.isInstanceOf(SoapMessage.class, messageContext.getResponse());
		MessageAddressingProperties requestMap = this.version
			.getMessageAddressingProperties((SoapMessage) messageContext.getRequest());
		EndpointReference replyEpr = (!isFault) ? requestMap.getReplyTo() : requestMap.getFaultTo();
		if (handleNoneAddress(messageContext, replyEpr)) {
			return false;
		}
		SoapMessage reply = (SoapMessage) messageContext.getResponse();
		URI replyMessageId = getMessageId(reply);
		URI action = (isFault) ? this.faultAction : this.replyAction;
		MessageAddressingProperties replyMap = requestMap.getReplyProperties(replyEpr, action, replyMessageId);
		this.version.addAddressingHeaders(reply, replyMap);
		if (handleAnonymousAddress(messageContext, replyEpr)) {
			return true;
		}
		else {
			sendOutOfBand(messageContext, replyEpr);
			return false;
		}
	}

	@Contract("_, null -> true")
	private boolean handleNoneAddress(MessageContext messageContext, @Nullable EndpointReference replyEpr) {
		if (replyEpr == null || this.version.hasNoneAddress(replyEpr)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Request [" + messageContext.getRequest() + "] has [" + replyEpr
						+ "] reply address; reply [" + messageContext.getResponse() + "] discarded");
			}
			messageContext.clearResponse();
			return true;
		}
		return false;
	}

	private boolean handleAnonymousAddress(MessageContext messageContext, EndpointReference replyEpr) {
		if (this.version.hasAnonymousAddress(replyEpr)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Request [" + messageContext.getRequest() + "] has [" + replyEpr
						+ "] reply address; sending in-band reply [" + messageContext.getResponse() + "]");
			}
			return true;
		}
		return false;
	}

	private void sendOutOfBand(MessageContext messageContext, EndpointReference replyEpr) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Request [" + messageContext.getRequest() + "] has [" + replyEpr
					+ "] reply address; sending out-of-band reply [" + messageContext.getResponse() + "]");
		}

		boolean supported = false;
		for (WebServiceMessageSender messageSender : this.messageSenders) {
			if (messageSender.supports(replyEpr.getAddress())) {
				supported = true;
				try (WebServiceConnection connection = messageSender.createConnection(replyEpr.getAddress())) {
					connection.send(messageContext.getResponse());
					break;
				}
				finally {
					messageContext.clearResponse();
				}
			}
		}
		if (!supported && logger.isWarnEnabled()) {
			logger.warn("Could not send out-of-band response to [" + replyEpr.getAddress() + "]. "
					+ "Configure WebServiceMessageSenders which support this uri.");
		}
	}

	private URI getMessageId(SoapMessage response) {
		URI responseMessageId = this.messageIdStrategy.newMessageId(response);
		if (logger.isTraceEnabled()) {
			logger.trace("Generated reply MessageID [" + responseMessageId + "] for [" + response + "]");
		}
		return responseMessageId;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, @Nullable Exception ex) {
	}

	@Override
	public boolean understands(SoapHeaderElement header) {
		return this.version.understands(header);
	}

}
