/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap;

import javax.xml.namespace.QName;

/**
 * Interface that defines a specific version. Contains properties for elements that make up a soap envelope.
 *
 * @author Arjen Poutsma
 * @see #SOAP_11
 * @see #SOAP_12
 */
public interface SoapVersion {

    /**
     * Represents version 1.1 of the SOAP specification.
     *
     * @see <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/">SOAP 1.1 specification</a>
     */
    public static final SoapVersion SOAP_11 = new SoapVersion() {

        private static final String ENVELOPE_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";

        private final QName ENVELOPE_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Envelope", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName HEADER_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Header", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName BODY_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Body", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Fault", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private static final String NEXT_ROLE_URI = "http://schemas.xmlsoap.org/soap/actor/next";

        private static final String CONTENT_TYPE = "text/xml";

        private final QName MUST_UNDERSTAND_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "mustUnderstand", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName ACTOR_NAME = new QName(ENVELOPE_NAMESPACE_URI, "actor", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private static final String DEFAULT_ENVELOPE_NAMESPACE_PREFIX = "soapenv";

        private final QName CLIENT_FAULT_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Client", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName SERVER_FAULT_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Server", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        public QName getBodyName() {
            return BODY_NAME;
        }

        public QName getEnvelopeName() {
            return ENVELOPE_NAME;
        }

        public String getEnvelopeNamespaceUri() {
            return ENVELOPE_NAMESPACE_URI;
        }

        public String getDefaultEnvelopeNamespacePrefix() {
            return DEFAULT_ENVELOPE_NAMESPACE_PREFIX;
        }

        public QName getFaultName() {
            return FAULT_NAME;
        }

        public QName getHeaderName() {
            return HEADER_NAME;
        }

        public String getNextRoleUri() {
            return NEXT_ROLE_URI;
        }

        public String getNoneRoleUri() {
            return "";
        }

        public QName getReceiverFaultName() {
            return SERVER_FAULT_NAME;
        }

        public String getUltimateReceiverRoleUri() {
            return "";
        }

        public QName getRoleName() {
            return ACTOR_NAME;
        }

        public QName getSenderFaultName() {
            return CLIENT_FAULT_NAME;
        }

        public String getContentType() {
            return CONTENT_TYPE;
        }

        public QName getMustUnderstandName() {
            return MUST_UNDERSTAND_NAME;
        }
    };

    /**
     * Represents version 1.2 of the SOAP specification.
     *
     * @see <a href="http://www.w3.org/TR/soap12-part0/">SOAP 1.2 specification</a>
     */
    public static final SoapVersion SOAP_12 = new SoapVersion() {

        private static final String ENVELOPE_NAMESPACE_URI = "http://www.w3.org/2003/05/soap-envelope";

        private final QName ENVELOPE_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Envelope", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName HEADER_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Header", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName BODY_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Body", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Fault", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private static final String NEXT_ROLE_URI = ENVELOPE_NAMESPACE_URI + "/role/next";

        private static final String NONE_ROLE_URI = ENVELOPE_NAMESPACE_URI + "/role/none";

        private static final String ULTIMATE_RECEIVER_ROLE_URI = ENVELOPE_NAMESPACE_URI + "/role/ultimateReceiver";

        private static final String CONTENT_TYPE = "application/soap+xml";

        private final QName MUST_UNDERSTAND_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "mustUnderstand", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName ROLE_NAME = new QName(ENVELOPE_NAMESPACE_URI, "role", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private static final String DEFAULT_ENVELOPE_NAMESPACE_PREFIX = "soapenv";

        private final QName SENDER_FAULT_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Sender", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        private final QName RECEIVER_FAULT_NAME =
                new QName(ENVELOPE_NAMESPACE_URI, "Receiver", DEFAULT_ENVELOPE_NAMESPACE_PREFIX);

        public QName getBodyName() {
            return BODY_NAME;
        }

        public QName getEnvelopeName() {
            return ENVELOPE_NAME;
        }

        public String getEnvelopeNamespaceUri() {
            return ENVELOPE_NAMESPACE_URI;
        }

        public String getDefaultEnvelopeNamespacePrefix() {
            return DEFAULT_ENVELOPE_NAMESPACE_PREFIX;
        }

        public QName getFaultName() {
            return FAULT_NAME;
        }

        public QName getHeaderName() {
            return HEADER_NAME;
        }

        public String getNextRoleUri() {
            return NEXT_ROLE_URI;
        }

        public String getNoneRoleUri() {
            return NONE_ROLE_URI;
        }

        public QName getReceiverFaultName() {
            return RECEIVER_FAULT_NAME;
        }

        public String getUltimateReceiverRoleUri() {
            return ULTIMATE_RECEIVER_ROLE_URI;
        }

        public QName getRoleName() {
            return ROLE_NAME;
        }

        public QName getSenderFaultName() {
            return SENDER_FAULT_NAME;
        }

        public String getContentType() {
            return CONTENT_TYPE;
        }

        public QName getMustUnderstandName() {
            return MUST_UNDERSTAND_NAME;
        }
    };

    /**
     * Returns the qualified name for a SOAP body.
     */
    QName getBodyName();

    /**
     * Returns the <code>Content-Type</code> MIME header for a SOAP message.
     */
    String getContentType();

    /**
     * Returns the default namespace prefix for the SOAP envelope namespace.
     */
    String getDefaultEnvelopeNamespacePrefix();

    /**
     * Returns the qualified name for a SOAP envelope.
     */
    QName getEnvelopeName();

    /**
     * Returns the namespace URI for the SOAP envelope namespace.
     */
    String getEnvelopeNamespaceUri();

    /**
     * Returns the qualified name for a SOAP fault.
     */
    QName getFaultName();

    /**
     * Returns the qualified name for a SOAP header.
     */
    QName getHeaderName();

    /**
     * Returns the qualified name of the SOAP <code>MustUnderstand</code> attribute and SOAP Fault value.
     */
    QName getMustUnderstandName();

    /**
     * Returns the URI indicating that a header element is intended for the next SOAP application that processes the
     * message.
     */
    String getNextRoleUri();

    /**
     * Returns the URI indicating that a header element should never be directly processed.
     */
    String getNoneRoleUri();

    /**
     * Returns the qualified name of the Receiver/Server SOAP Fault value.
     */
    QName getReceiverFaultName();

    /**
     * Returns the qualified name of the SOAP <code>actor</code>/<code>role</code> attribute.
     */
    QName getRoleName();

    /**
     * Returns the qualified name of the Sender/Client SOAP Fault value.
     */
    QName getSenderFaultName();

    /**
     * Returns the URI indicating that a header element should only be processed by nodes acting as the ultimate
     * receiver of a message.
     */
    String getUltimateReceiverRoleUri();
}
