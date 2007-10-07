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

/*
    public MessageAddressingProperties(String to, EndpointReference replyTo, String action, String messageId) {
        this.to = to;
        this.replyTo = replyTo;
        this.action = action;
        this.messageId = messageId;
        this.from = null;
        this.faultTo = null;
        this.relatesTo = null;
        this.referenceProperties = Collections.EMPTY_LIST;
        this.referenceParameters = Collections.EMPTY_LIST;
    }
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

/*
    private MessageAddressingProperties(String to,
                                       String action,
                                       String messageId,
                                       String relatesTo,
                                       List referenceProperties,
                                       List referenceParameters) {
        this.to = to;
        this.action = action;
        this.messageId = messageId;
        this.relatesTo = relatesTo;
        this.referenceProperties = referenceProperties;
        this.referenceParameters = referenceParameters;
        this.from = null;
        this.replyTo = null;
        this.faultTo = null;
    }
*/

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

    public String getTo() {
        return to;
    }

    public EndpointReference getFrom() {
        return from;
    }

    public EndpointReference getReplyTo() {
        return replyTo != null ? replyTo : getFrom();
    }

    public EndpointReference getFaultTo() {
        return faultTo != null ? faultTo : getReplyTo();
    }

    public String getAction() {
        return action;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getRelatesTo() {
        return relatesTo;
    }

    public List getReferenceProperties() {
        return Collections.unmodifiableList(referenceProperties);
    }

    public List getReferenceParameters() {
        return Collections.unmodifiableList(referenceParameters);
    }

    /**
     * Indicates whether the given {@link MessageAddressingProperties} are valid, i.e. whether all required elements are
     * listed.
     */
    public boolean isValid() {
        return StringUtils.hasLength(to) && StringUtils.hasLength(action) &&
                !(replyTo != null && !StringUtils.hasLength(messageId)) &&
                !(faultTo != null && !StringUtils.hasLength(messageId));

    }

    public MessageAddressingProperties getResponseProperties(EndpointReference epr, String action, String messageId) {
        return new MessageAddressingProperties(epr, action, messageId, this.messageId);
    }

}
