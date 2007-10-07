package org.springframework.ws.soap.addressing;

import javax.xml.namespace.QName;

/**
 * Implements the August 2004 edition of the WS-Addressing specification. This version of the specification is used by
 * Microsoft's Web Services Enhancements (WSE) 3.0.
 *
 * @author Arjen Poutsma
 * @see <a href="http://msdn.microsoft.com/ws/2004/08/ws-addressing/">Web Services Addressing, August 2004</a>
 * @see <a href="http://msdn.microsoft.com/webservices/webservices/building/wse/">Web Services Enhancements</a>
 * @since 1.1.0
 */
public class WsAddressing200408Interceptor extends AbstractVersionBasedWsAddressingInterceptor {

    protected WsAddressing200408Interceptor() {
        super(new WsAddressing200408());
    }

    private static class WsAddressing200408 extends AbstractWsAddressingVersion {

        private static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

        public String getNamespaceUri() {
            return NAMESPACE_URI;
        }

        /*
         * Message addressing properties
         */

        public String getRelationshipReply() {
            return getNamespacePrefix() + ":Reply";
        }

        /*
         * Address URIs
         */

        public String getAnonymousUri() {
            return NAMESPACE_URI + "/role/anonymous";
        }

        public String getNoneUri() {
            return null;
        }

        /*
         * Faults
         */

        public QName getMessageAddressingHeaderRequiredFaultSubcode() {
            return new QName(NAMESPACE_URI, "MessageInformationHeaderRequired", getNamespacePrefix());
        }

        public String getMessageAddressingHeaderRequiredFaultReason() {
            return "A required message information header, To, MessageID, or Action, is not present.";
        }
    }
}
