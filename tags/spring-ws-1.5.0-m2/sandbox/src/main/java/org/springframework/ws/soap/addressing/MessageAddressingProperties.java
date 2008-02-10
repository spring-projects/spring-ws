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

package org.springframework.ws.soap.addressing;

import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Represents a set of Message Addressing Properties, as defined in the WS-Addressing specification.
 * <p/>
 * In earlier versions of the spec, these properties were called Message Information Headers.
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.w3.org/TR/ws-addr-core/#msgaddrprops">Message Addressing Properties</a>
 * @since 1.1.0
 */
public final class MessageAddressingProperties {

    private final String to;

    private final EndpointReference from;

    private final EndpointReference replyTo;

    private final EndpointReference faultTo;

    private final String action;

    private final String messageId;

    private final String relatesTo;

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
    public MessageAddressingProperties(String to,
                                       EndpointReference from,
                                       EndpointReference replyTo,
                                       EndpointReference faultTo,
                                       String action,
                                       String messageId) {
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

    private MessageAddressingProperties(EndpointReference epr, String action, String messageId, String relatesTo) {
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
    public String getTo() {
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

    /** Returns the value of the fault endpoint property. Defaults to {@link #getReplyTo()} if no fault endpoint is set. */
    public EndpointReference getFaultTo() {
        return faultTo != null ? faultTo : getReplyTo();
    }

    /** Returns the value of the action property. */
    public String getAction() {
        return action;
    }

    /** Returns the value of the message id property. */
    public String getMessageId() {
        return messageId;
    }

    /** Returns the value of the relationship property. */
    public String getRelatesTo() {
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
     * Indicates whether is {@link MessageAddressingProperties} is valid, i.e. whether all required elements are listed.
     * Returns <code>true</code> if the destination and action properties have been set, and if a reply or fault
     * endpoint has been set, also checks for the message id.
     */
    public boolean isValid() {
        return StringUtils.hasLength(to) && StringUtils.hasLength(action) &&
                !(replyTo != null && !StringUtils.hasLength(messageId)) &&
                !(faultTo != null && !StringUtils.hasLength(messageId));

    }

    public MessageAddressingProperties getResponseProperties(EndpointReference epr, String action, String messageId) {
        return new MessageAddressingProperties(epr, action, messageId, this.messageId);
    }

    /**
     * Indicates whether is {@link MessageAddressingProperties} has all required properties. Returns <code>true</code>
     * if the destination and action properties have been set, and if a reply or fault endpoint has been set, also
     * checks for the message id.
     */
    public boolean hasRequiredProperties() {
        return StringUtils.hasLength(to) && StringUtils.hasLength(action) &&
                !(replyTo != null && !StringUtils.hasLength(messageId)) &&
                !(faultTo != null && !StringUtils.hasLength(messageId));
    }
}
