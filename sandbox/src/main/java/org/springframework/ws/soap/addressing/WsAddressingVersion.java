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

import javax.xml.namespace.QName;

/**
 * Defines the contract for a specific version of the WS-Addressing specification.
 *
 * @author Arjen Poutsma
 */
public interface WsAddressingVersion {

    /** Returns the WS-Addressing namespace handled by this specification. */
    String getNamespaceUri();

    /** Returns the prefix associated with the WS-Addressing namespace handled by this specification. */
    String getNamespacePrefix();

    /*
     * Message addressing properties
     */

    /** Returns the qualified name of the <code>To</code> addressing header. */
    QName getToName();

    /** Returns the qualified name of the <code>From</code> addressing header. */
    QName getFromName();

    /** Returns the qualified name of the <code>ReplyTo</code> addressing header. */
    QName getReplyToName();

    /** Returns the qualified name of the <code>FaultTo</code> addressing header. */
    QName getFaultToName();

    /** Returns the qualified name of the <code>Action</code> addressing header. */
    QName getActionName();

    /** Returns the qualified name of the <code>MessageID</code> addressing header. */
    QName getMessageIdName();

    /** Returns the qualified name of the <code>RelatesTo</code> addressing header. */
    QName getRelatesToName();

    /**
     * Returns the qualified name of the <code>Relationship</code> addressing attribute.
     *
     * @see #getRelationshipReply()
     */
    QName getRelationshipTypeName();

    /**
     * Returns the value of the <code>RelationshipType</code> attribute indicating a reply to the related message.
     *
     * @see #getRelationshipTypeName()
     */
    String getRelationshipReply();

    /**
     * Returns the qualified name of the <code>ReferenceProperties</code> in the endpoint reference. Returns
     * <code>null</code> when reference properties are not supported by this version of the spec.
     */
    QName getReferencePropertiesName();

    /**
     * Returns the qualified name of the <code>ReferenceParameters</code> in the endpoint reference. Returns
     * <code>null</code> when reference parameters are not supported by this version of the spec.
     */
    QName getReferenceParametersName();

    /*
     * Endpoint Reference
     */

    /** The qualified name of the <code>Address</code> in <code>EndpointReference</code>. */
    QName getAddressName();

    /** The qualified name of the <code>Metadata</code> in <code>EndpointReference</code>. */
    QName getMetadataName();

    /*
     * Address URIs
     */

    /** Returns the anonymous URI. */
    String getAnonymousUri();

    /** Returns the none URI, or <code>null</code> if the spec does not define it. */
    String getNoneUri();

    /*
     * Faults
     */

    /** Returns the qualified name of the fault subcode that indicates that a header is missing. */
    QName getMessageAddressingHeaderRequiredFaultSubcode();

    /** Returns the reason of the fault that indicates that a header is missing. */
    String getMessageAddressingHeaderRequiredFaultReason();

}
