package org.springframework.ws.soap.addressing;

import javax.xml.namespace.QName;

/**
 * Abstract implementation of the {@link WsAddressingVersion} interface.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractWsAddressingVersion implements WsAddressingVersion {

    public String getNamespacePrefix() {
        return "wsa";
    }

    /*
     * Message addressing properties
     */

    public QName getToName() {
        return new QName(getNamespaceUri(), "To", getNamespacePrefix());
    }

    public QName getFromName() {
        return new QName(getNamespaceUri(), "From", getNamespacePrefix());
    }

    public QName getReplyToName() {
        return new QName(getNamespaceUri(), "ReplyTo", getNamespacePrefix());
    }

    public QName getFaultToName() {
        return new QName(getNamespaceUri(), "FaultTo", getNamespacePrefix());
    }

    public QName getActionName() {
        return new QName(getNamespaceUri(), "Action", getNamespacePrefix());
    }

    public QName getMessageIdName() {
        return new QName(getNamespaceUri(), "MessageID", getNamespacePrefix());
    }

    public QName getRelatesToName() {
        return new QName(getNamespaceUri(), "RelatesTo", getNamespacePrefix());
    }

    public QName getRelationshipTypeName() {
        return new QName(getNamespaceUri(), "RelationshipType", getNamespacePrefix());
    }

    public QName getReferencePropertiesName() {
        return new QName(getNamespaceUri(), "ReferenceProperties", getNamespacePrefix());
    }

    public QName getReferenceParametersName() {
        return new QName(getNamespaceUri(), "ReferenceParameters", getNamespacePrefix());
    }

    /*
     * Endpoint Reference
     */

    public QName getAddressName() {
        return new QName(getNamespaceUri(), "Address", getNamespacePrefix());
    }

    public QName getMetadataName() {
        return new QName(getNamespaceUri(), "Metadata", getNamespacePrefix());
    }

}
