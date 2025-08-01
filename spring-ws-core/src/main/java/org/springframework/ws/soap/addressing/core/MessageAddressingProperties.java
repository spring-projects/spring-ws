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

package org.springframework.ws.soap.addressing.core;

import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;

/**
 * Represents a set of Message Addressing Properties, as defined in the WS-Addressing
 * specification.
 * <p>
 * In earlier versions of the spec, these properties were called Message Information
 * Headers.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @see <a href="http://www.w3.org/TR/ws-addr-core/#msgaddrprops">Message Addressing
 * Properties</a>
 */
public final class MessageAddressingProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = -6980663311446506672L;

	private final @Nullable URI to;

	private final @Nullable EndpointReference from;

	private final @Nullable EndpointReference replyTo;

	private final @Nullable EndpointReference faultTo;

	private final @Nullable URI action;

	private final @Nullable URI messageId;

	private final @Nullable URI relatesTo;

	private final List<Node> referenceProperties;

	private final List<Node> referenceParameters;

	/**
	 * Constructs a new {@link MessageAddressingProperties} with the given parameters.
	 * @param to the value of the destination property
	 * @param from the value of the source endpoint property
	 * @param replyTo the value of the reply endpoint property
	 * @param faultTo the value of the fault endpoint property
	 * @param action the value of the action property
	 * @param messageId the value of the message id property
	 */
	public MessageAddressingProperties(@Nullable URI to, @Nullable EndpointReference from,
			@Nullable EndpointReference replyTo, @Nullable EndpointReference faultTo, @Nullable URI action,
			@Nullable URI messageId) {
		this.to = to;
		this.from = from;
		this.replyTo = replyTo;
		this.faultTo = faultTo;
		this.action = action;
		this.messageId = messageId;
		this.relatesTo = null;
		this.referenceProperties = Collections.emptyList();
		this.referenceParameters = Collections.emptyList();
	}

	/**
	 * Constructs a new {@link MessageAddressingProperties} that forms a reply to the
	 * given EPR.
	 * @param epr the endpoint reference to create a reply for
	 * @param action the value of the action property
	 * @param messageId the value of the message id property
	 * @param relatesTo the value of the relates to property
	 */
	private MessageAddressingProperties(EndpointReference epr, @Nullable URI action, URI messageId,
			@Nullable URI relatesTo) {
		this.to = epr.getAddress();
		this.action = action;
		this.messageId = messageId;
		this.relatesTo = relatesTo;
		this.referenceParameters = epr.getReferenceParameters();
		this.referenceProperties = epr.getReferenceProperties();
		this.from = null;
		this.replyTo = null;
		this.faultTo = null;
	}

	/** Returns the value of the destination property. */
	public @Nullable URI getTo() {
		return this.to;
	}

	/** Returns the value of the source endpoint property. */
	public @Nullable EndpointReference getFrom() {
		return this.from;
	}

	/** Returns the value of the reply endpoint property. */
	public @Nullable EndpointReference getReplyTo() {
		return this.replyTo;
	}

	/** Returns the value of the fault endpoint property. */
	public @Nullable EndpointReference getFaultTo() {
		return this.faultTo;
	}

	/** Returns the value of the action property. */
	public @Nullable URI getAction() {
		return this.action;
	}

	/** Returns the value of the message id property. */
	public @Nullable URI getMessageId() {
		return this.messageId;
	}

	/** Returns the value of the relationship property. */
	public @Nullable URI getRelatesTo() {
		return this.relatesTo;
	}

	/** Returns the endpoint properties. Returns an empty list of none are set. */
	public List<Node> getReferenceProperties() {
		return Collections.unmodifiableList(this.referenceProperties);
	}

	/** Returns the endpoint parameters. Returns an empty list of none are set. */
	public List<Node> getReferenceParameters() {
		return Collections.unmodifiableList(this.referenceParameters);
	}

	/**
	 * Creates a {@link MessageAddressingProperties} that can be used for creating a reply
	 * to the given {@link EndpointReference}. The {@link #getTo() destination} property
	 * will be populated with the {@link EndpointReference#getAddress() address} of the
	 * given EPR, and the {@link #getRelatesTo() relationship} property will be set to the
	 * {@link #getMessageId() message id} property of this instance. the action is
	 * specified, the
	 * @param epr the endpoint reference to create a reply to
	 * @param action the action
	 */
	public MessageAddressingProperties getReplyProperties(EndpointReference epr, @Nullable URI action, URI messageId) {
		return new MessageAddressingProperties(epr, action, messageId, this.messageId);
	}

}
