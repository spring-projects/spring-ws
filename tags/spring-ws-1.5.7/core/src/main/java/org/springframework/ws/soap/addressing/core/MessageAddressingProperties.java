/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.addressing.core;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Represents a set of Message Addressing Properties, as defined in the WS-Addressing specification.
 * <p/>
 * In earlier versions of the spec, these properties were called Message Information Headers.
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.w3.org/TR/ws-addr-core/#msgaddrprops">Message Addressing Properties</a>
 * @since 1.5.0
 */
public final class MessageAddressingProperties {

    private final URI to;

    private final EndpointReference from;

    private final EndpointReference replyTo;

    private final EndpointReference faultTo;

    private final URI action;

    private final URI messageId;

    private final URI relatesTo;

    private final List referenceProperties;

    private final List referenceParameters;

    /**
     * Constructs a new {@link MessageAddressingProperties} with the given parameters.
     *
     * @param to        the value of the destination property
     * @param from      the value of the source endpoint property
     * @param replyTo   the value of the reply endpoint property
     * @param faultTo   the value of the fault endpoint property
     * @param action    the value of the action property
     * @param messageId the value of the message id property
     */
    public MessageAddressingProperties(URI to,
                                       EndpointReference from,
                                       EndpointReference replyTo,
                                       EndpointReference faultTo,
                                       URI action,
                                       URI messageId) {
        this.to = to;
        this.from = from;
        this.replyTo = replyTo;
        this.faultTo = faultTo;
        this.action = action;
        this.messageId = messageId;
        this.relatesTo = null;
        this.referenceProperties = Collections.EMPTY_LIST;
        this.referenceParameters = Collections.EMPTY_LIST;
    }

    /**
     * Constructs a new {@link MessageAddressingProperties} that forms a reply to the given EPR.
     *
     * @param epr       the endpoint reference to create a reply for
     * @param action    the value of the action property
     * @param messageId the value of the message id property
     * @param relatesTo the value of the relates to property
     */
    private MessageAddressingProperties(EndpointReference epr, URI action, URI messageId, URI relatesTo) {
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
    public URI getTo() {
        return to;
    }

    /** Returns the value of the source endpoint property. */
    public EndpointReference getFrom() {
        return from;
    }

    /** Returns the value of the reply endpoint property. */
    public EndpointReference getReplyTo() {
        return replyTo;
    }

    /** Returns the value of the fault endpoint property. */
    public EndpointReference getFaultTo() {
        return faultTo;
    }

    /** Returns the value of the action property. */
    public URI getAction() {
        return action;
    }

    /** Returns the value of the message id property. */
    public URI getMessageId() {
        return messageId;
    }

    /** Returns the value of the relationship property. */
    public URI getRelatesTo() {
        return relatesTo;
    }

    /** Returns the endpoint properties. Returns an empty list of none are set. */
    public List getReferenceProperties() {
        return Collections.unmodifiableList(referenceProperties);
    }

    /** Returns the endpoint parameters. Returns an empty list of none are set. */
    public List getReferenceParameters() {
        return Collections.unmodifiableList(referenceParameters);
    }

    /**
     * Creates a {@link MessageAddressingProperties} that can be used for creating a reply to the given {@link
     * EndpointReference}. The {@link #getTo() destination} property will be populated with the {@link
     * EndpointReference#getAddress() address} of the given EPR, and the {@link #getRelatesTo() relationship} property
     * will be set to the {@link #getMessageId() message id} property of this instance. the action is specified, the
     *
     * @param epr    the endpoint reference to create a reply to
     * @param action the action
     */
    public MessageAddressingProperties getReplyProperties(EndpointReference epr, URI action, URI messageId) {
        return new MessageAddressingProperties(epr, action, messageId, this.messageId);
    }
}
