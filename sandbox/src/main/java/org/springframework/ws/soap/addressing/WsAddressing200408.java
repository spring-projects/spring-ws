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
 * Implements the August 2004 edition of the WS-Addressing specification. This version of the specification is used by
 * Microsoft's Web Service Enhancements (WSE) 3.0.
 *
 * @author Arjen Poutsma
 * @see <a href="http://msdn.microsoft.com/ws/2004/08/ws-addressing/">Web Services Addressing, August 2004</a>
 * @see <a href="http://msdn.microsoft.com/webservices/webservices/building/wse/">Web Service Enhancements</a>
 */
public class WsAddressing200408 implements WsAddressingVersion {

    private static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

    private static final String NAMESPACE_PREFIX = "wsa";

    private static final QName TO = new QName(NAMESPACE_URI, "To", NAMESPACE_PREFIX);

    private static final QName REPLY_TO = new QName(NAMESPACE_URI, "ReplyTo", NAMESPACE_PREFIX);

    private static final QName FROM = new QName(NAMESPACE_URI, "From", NAMESPACE_PREFIX);

    private static final QName FAULT_TO = new QName(NAMESPACE_URI, "FaultTo", NAMESPACE_PREFIX);

    private static final QName ACTION = new QName(NAMESPACE_URI, "Action", NAMESPACE_PREFIX);

    private static final QName MESSAGE_ID = new QName(NAMESPACE_URI, "MessageID", NAMESPACE_PREFIX);

    private static final QName RELATES_TO = new QName(NAMESPACE_URI, "RelatesTo", NAMESPACE_PREFIX);

    private static final QName RELATIONSHIP_REPLY = new QName(NAMESPACE_URI, "Reply", NAMESPACE_PREFIX);

    private static final QName RELATIONSHIP_TYPE = new QName(NAMESPACE_URI, "RelationshipType", NAMESPACE_PREFIX);

    private static final QName MESSAGE_INFORMATION_HEADER_REQUIRED =
            new QName(NAMESPACE_URI, "MessageInformationHeaderRequired", NAMESPACE_PREFIX);

    private static final QName DESTINATION_UNREACHABLE =
            new QName(NAMESPACE_URI, "DestinationUnreachable", NAMESPACE_PREFIX);

    private static final QName ACTION_NOT_SUPPORTED_NAME =
            new QName(NAMESPACE_URI, "ActionNotSupported", NAMESPACE_PREFIX);

    private static final QName ADDRESS = new QName(NAMESPACE_URI, "Address", NAMESPACE_PREFIX);

    private static final QName REFERENCE_PARAMETERS = new QName(NAMESPACE_URI, "ReferenceParameters", NAMESPACE_PREFIX);

    private static final QName REFERENCE_PROPERTIES = new QName(NAMESPACE_URI, "ReferenceProperties", NAMESPACE_PREFIX);

    public String getNamespaceUri() {
        return NAMESPACE_URI;
    }

    public String getNamespacePrefix() {
        return NAMESPACE_PREFIX;
    }

    public String getAnonymousUri() {
        return NAMESPACE_URI + "/role/anonymous";
    }

    public String getNoneUri() {
        return null;
    }

    public QName getToName() {
        return TO;
    }

    public QName getFromName() {
        return FROM;
    }

    public QName getReplyToName() {
        return REPLY_TO;
    }

    public QName getFaultToName() {
        return FAULT_TO;
    }

    public QName getActionName() {
        return ACTION;
    }

    public QName getMessageIdName() {
        return MESSAGE_ID;
    }

    public QName getRelatesToName() {
        return RELATES_TO;
    }

    public QName getRelationshipReplyName() {
        return RELATIONSHIP_REPLY;
    }

    public QName getAddressName() {
        return ADDRESS;
    }

    public QName getRelationshipTypeName() {
        return RELATIONSHIP_TYPE;
    }

    public QName getReferenceParametersName() {
        return REFERENCE_PARAMETERS;
    }

    public QName getReferencePropertiesName() {
        return REFERENCE_PROPERTIES;
    }

    public QName getMessageHeaderRequiredName() {
        return MESSAGE_INFORMATION_HEADER_REQUIRED;
    }

    public String getMessageHeaderRequiredText() {
        return "A required message information header, To, MessageID, or Action, is not present.";
    }

    public QName getDestinationUnreachableName() {
        return DESTINATION_UNREACHABLE;
    }

    public String getDestinationUnreachableText() {
        return "No route can be determined to reach the destination role defined by the WS-Addressing To.";
    }

    public QName getActionNotSupportedName() {
        return ACTION_NOT_SUPPORTED_NAME;
    }

    public String getActionNotSupportedText(String action) {
        return "The " + action + " cannot be processed at the receiver.";
    }

    public String toString() {
        return NAMESPACE_URI;
    }
}
