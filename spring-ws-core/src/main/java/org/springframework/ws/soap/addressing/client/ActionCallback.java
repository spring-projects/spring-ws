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

package org.springframework.ws.soap.addressing.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.AddressingVersion;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

/**
 * {@link WebServiceMessageCallback} implementation that sets the WS-Addressing
 * {@code Action} header on the message.
 * <p>
 * A usage example with {@link org.springframework.ws.client.core.WebServiceTemplate}:
 *
 * <pre>
 * WebServiceTemplate template = new WebServiceTemplate(messageFactory);
 * Result result = new DOMResult();
 * template.sendSourceAndReceiveToResult(new StringSource("&lt;content xmlns=\"http://tempuri.org\"/&gt;"),
 * 		new ActionCallback(new URI("http://tempuri.org/Action")), result);
 * </pre>
 *
 * @author Arjen Poutsma
 * @author Leandro Quiroga
 * @since 1.0.0
 */
public class ActionCallback implements WebServiceMessageCallback {

	private final AddressingVersion version;

	private final URI action;

	private @Nullable final URI to;

	private boolean shouldInitializeTo;

	private MessageIdStrategy messageIdStrategy;

	private @Nullable EndpointReference from;

	private @Nullable EndpointReference replyTo;

	private @Nullable EndpointReference faultTo;

	/**
	 * Create a new {@code ActionCallback} with the given {@code Action}.
	 * <p>
	 * The {@code To} header of the outgoing message will reflect the
	 * {@link org.springframework.ws.transport.WebServiceConnection#getUri() connection
	 * URI}.
	 * <p>
	 * The {@link AddressingVersion} is set to {@link Addressing10}.
	 * @param action the value of the action property to set
	 */
	public ActionCallback(String action) throws URISyntaxException {
		this(new URI(action), new Addressing10(), null);
	}

	/**
	 * Create a new {@code ActionCallback} with the given {@code Action}.
	 * <p>
	 * The {@code To} header of the outgoing message will reflect the
	 * {@link org.springframework.ws.transport.WebServiceConnection#getUri() connection
	 * URI}.
	 * <p>
	 * The {@link AddressingVersion} is set to {@link Addressing10}.
	 * @param action the value of the action property to set
	 */
	public ActionCallback(URI action) {
		this(action, new Addressing10(), null);
	}

	/**
	 * Create a new {@code ActionCallback} with the given version and {@code Action}.
	 * <p>
	 * The {@code To} header of the outgoing message will reflect the
	 * {@link org.springframework.ws.transport.WebServiceConnection#getUri() connection
	 * URI}.
	 * @param action the value of the action property to set
	 * @param version the WS-Addressing version to use
	 */
	public ActionCallback(URI action, AddressingVersion version) {
		this(action, version, null);
	}

	/**
	 * Create a new {@code ActionCallback} with the given version, {@code Action}, and
	 * optional {@code To}.
	 * @param action the value of the action property
	 * @param version the WS-Addressing version to use
	 * @param to the value of the destination property
	 */
	public ActionCallback(URI action, AddressingVersion version, @Nullable URI to) {
		Assert.notNull(action, "'action' must not be null");
		Assert.notNull(version, "'version' must not be null");
		this.action = action;
		this.version = version;
		this.to = to;
		this.messageIdStrategy = new UuidMessageIdStrategy();
	}

	/**
	 * Returns the WS-Addressing version.
	 */
	public AddressingVersion getVersion() {
		return this.version;
	}

	/**
	 * Returns the message id strategy used for creating WS-Addressing MessageIds.
	 * <p>
	 * By default, the {@link UuidMessageIdStrategy} is used.
	 */
	public MessageIdStrategy getMessageIdStrategy() {
		return this.messageIdStrategy;
	}

	/**
	 * Sets the message id strategy used for creating WS-Addressing MessageIds.
	 * <p>
	 * By default, the {@link UuidMessageIdStrategy} is used.
	 */
	public void setMessageIdStrategy(MessageIdStrategy messageIdStrategy) {
		Assert.notNull(messageIdStrategy, "'messageIdStrategy' must not be null");
		this.messageIdStrategy = messageIdStrategy;
	}

	/**
	 * Returns the {@code Action}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getAction()
	 */
	public URI getAction() {
		return this.action;
	}

	/**
	 * Returns the {@code From}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getFrom()
	 */
	public @Nullable EndpointReference getFrom() {
		return this.from;
	}

	/**
	 * Sets the {@code From}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getFrom()
	 */
	public void setFrom(EndpointReference from) {
		this.from = from;
	}

	/**
	 * Returns the {@code ReplyTo}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getReplyTo()
	 */
	public @Nullable EndpointReference getReplyTo() {
		return this.replyTo;
	}

	/**
	 * Sets the {@code ReplyTo}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getReplyTo()
	 */
	public void setReplyTo(EndpointReference replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * Returns the {@code FaultTo}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getFaultTo()
	 */
	public @Nullable EndpointReference getFaultTo() {
		return this.faultTo;
	}

	/**
	 * Sets the {@code FaultTo}.
	 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getFaultTo()
	 */
	public void setFaultTo(EndpointReference faultTo) {
		this.faultTo = faultTo;
	}

	/**
	 * Returns the {@code Destination} for outgoing messages.
	 * <p>
	 * Defaults to the
	 * {@link org.springframework.ws.transport.WebServiceConnection#getUri() connection
	 * URI} if no destination was set.
	 */
	protected @Nullable URI getTo() {
		if (this.to == null && (isToHeaderRequired() || this.shouldInitializeTo)) {
			TransportContext transportContext = TransportContextHolder.getTransportContext();
			if (transportContext != null && transportContext.getConnection() != null) {
				try {
					return transportContext.getConnection().getUri();
				}
				catch (URISyntaxException ex) {
					// ignore
				}
			}
			throw new IllegalStateException("Could not obtain connection URI from Transport Context");
		}
		else {
			return this.to;
		}
	}

	private boolean isToHeaderRequired() {
		return getVersion().isToHeaderRequired();
	}

	/**
	 * Set whether to initialize the {@code To} header by default or not.
	 */
	public void setShouldInitializeTo(boolean shouldInitializeTo) {
		this.shouldInitializeTo = shouldInitializeTo;
	}

	@Override
	public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {

		Assert.isInstanceOf(SoapMessage.class, message);
		SoapMessage soapMessage = (SoapMessage) message;
		URI messageId = getMessageIdStrategy().newMessageId(soapMessage);
		MessageAddressingProperties map = new MessageAddressingProperties(getTo(), getFrom(), getReplyTo(),
				getFaultTo(), getAction(), messageId);
		this.version.addAddressingHeaders(soapMessage, map);
	}

}
