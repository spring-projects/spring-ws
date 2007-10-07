package org.springframework.ws.soap.addressing;

import javax.xml.namespace.QName;

/** @author Arjen Poutsma */
public class WsAddressing200508Interceptor extends AbstractVersionBasedWsAddressingInterceptor {

    public WsAddressing200508Interceptor() {
        super(new WsAddressing200508());
    }

    private static class WsAddressing200508 extends AbstractWsAddressingVersion {

        private static final String NAMESPACE_URI = "http://www.w3.org/2005/08/addressing";

        public String getNamespaceUri() {
            return NAMESPACE_URI;
        }

        /*
         * Message addressing properties
         */

        public String getRelationshipReply() {
            return "http://www.w3.org/2005/08/addressing/reply";
        }

        public QName getReferencePropertiesName() {
            return null;
        }

        /*
         * Address URIs
         */

        public String getAnonymousUri() {
            return NAMESPACE_URI + "/anonymous";
        }

        public String getNoneUri() {
            return NAMESPACE_URI + "/none";
        }

        /*
         * Faults
         */

        public QName getMessageAddressingHeaderRequiredFaultSubcode() {
            return new QName(NAMESPACE_URI, "MessageAddressingHeaderRequired", getNamespacePrefix());
        }

        public String getMessageAddressingHeaderRequiredFaultReason() {
            return "A required header representing a Message Addressing Property is not present";
        }

    }

}