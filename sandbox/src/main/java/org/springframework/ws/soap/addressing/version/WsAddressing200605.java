package org.springframework.ws.soap.addressing.version;

import javax.xml.namespace.QName;

/**
 * Implements the May 2006 edition of the WS-Addressing specification. This version of the specification is used by
 * Microsoft's Windows Communication Foundation (WCF), and supported by Axis 1 and 2.
 *
 * @author Arjen Poutsma
 * @see <a href="http://www.w3.org/TR/2006/REC-ws-addr-core-20060509">Web Services Addressing, August 2004</a>
 * @since 1.1.0
 */

public class WsAddressing200605 extends AbstractWsAddressingVersion {

    private static final String NAMESPACE_URI = "http://www.w3.org/2005/08/addressing";

    protected String getNamespaceUri() {
        return NAMESPACE_URI;
    }

    protected QName getReferencePropertiesName() {
        return null;
    }

    protected final String getAnonymousUri() {
        return NAMESPACE_URI + "/anonymous";
    }

    protected final String getNoneUri() {
        return NAMESPACE_URI + "/none";
    }

    protected final QName getMessageAddressingHeaderRequiredFaultSubcode() {
        return new QName(NAMESPACE_URI, "MessageAddressingHeaderRequired", getNamespacePrefix());
    }

    protected final String getMessageAddressingHeaderRequiredFaultReason() {
        return "A required header representing a Message Addressing Property is not present";
    }

    protected QName getInvalidAddressingHeaderFaultSubcode() {
        return new QName(NAMESPACE_URI, "InvalidAddressingHeader", getNamespacePrefix());
    }

    protected String getInvalidAddressingHeaderFaultReason() {
        return "A header representing a Message Addressing Property is not valid and the message cannot be processed";
    }
}
