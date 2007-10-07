package org.springframework.ws.soap.addressing.version;

import javax.xml.namespace.QName;

/**
 * Implements the August 2004 edition of the WS-Addressing specification. This version of the specification is used by
 * Microsoft's Web Services Enhancements (WSE) 3.0, and supported by Axis 1 and 2, and XFire.
 *
 * @author Arjen Poutsma
 * @see <a href="http://msdn.microsoft.com/ws/2004/08/ws-addressing/">Web Services Addressing, August 2004</a>
 * @since 1.1.0
 */
public class WsAddressing200408 extends AbstractWsAddressingVersion {

    private static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

    protected final String getAnonymousUri() {
        return NAMESPACE_URI + "/role/anonymous";
    }

    protected final String getInvalidAddressingHeaderFaultReason() {
        return "A message information header is not valid and the message cannot be processed.";
    }

    protected final QName getInvalidAddressingHeaderFaultSubcode() {
        return new QName(NAMESPACE_URI, "InvalidMessageInformationHeader", getNamespacePrefix());
    }

    protected final String getMessageAddressingHeaderRequiredFaultReason() {
        return "A required message information header, To, MessageID, or Action, is not present.";
    }

    protected final QName getMessageAddressingHeaderRequiredFaultSubcode() {
        return new QName(NAMESPACE_URI, "MessageInformationHeaderRequired", getNamespacePrefix());
    }

    protected final String getNamespaceUri() {
        return NAMESPACE_URI;
    }

    protected final String getNoneUri() {
        return null;
    }
}
