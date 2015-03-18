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
 * Interface that defines a specific version of the SOAP specification. Contains properties for elements that make up a
 * soap envelope.
 *
 * @author Arjen Poutsma
 * @see #SOAP_11
 * @see #SOAP_12
 * @since 1.0.0
 */
public interface SoapVersion {

	/**
	 * Represents version 1.1 of the SOAP specification.
	 *
	 * @see <a href="http://www.w3.org/TR/2000/NOTE-SOAP-20000508/">SOAP 1.1 specification</a>
	 */
	SoapVersion SOAP_11 = new SoapVersion() {

		private static final String ENVELOPE_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";

		private static final String NEXT_ROLE_URI = "http://schemas.xmlsoap.org/soap/actor/next";

		private static final String CONTENT_TYPE = "text/xml";

		private QName ENVELOPE_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Envelope");

		private QName HEADER_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Header");

		private QName BODY_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Body");

		private QName FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Fault");

		private QName MUST_UNDERSTAND_ATTRIBUTE_NAME = new QName(ENVELOPE_NAMESPACE_URI, "mustUnderstand");

		private QName ACTOR_NAME = new QName(ENVELOPE_NAMESPACE_URI, "actor");

		private QName CLIENT_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Client");

		private QName SERVER_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Server");

		private QName MUST_UNDERSTAND_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "MustUnderstand");

		private QName VERSION_MISMATCH_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "VersionMismatch");

		public QName getBodyName() {
			return BODY_NAME;
		}

		public QName getEnvelopeName() {
			return ENVELOPE_NAME;
		}

		public String getEnvelopeNamespaceUri() {
			return ENVELOPE_NAMESPACE_URI;
		}

		public QName getFaultName() {
			return FAULT_NAME;
		}

		public QName getHeaderName() {
			return HEADER_NAME;
		}

		public String getNextActorOrRoleUri() {
			return NEXT_ROLE_URI;
		}

		public String getNoneActorOrRoleUri() {
			return "";
		}

		public QName getServerOrReceiverFaultName() {
			return SERVER_FAULT_NAME;
		}

		public String getUltimateReceiverRoleUri() {
			return "";
		}

		public QName getActorOrRoleName() {
			return ACTOR_NAME;
		}

		public QName getClientOrSenderFaultName() {
			return CLIENT_FAULT_NAME;
		}

		public String getContentType() {
			return CONTENT_TYPE;
		}

		public QName getMustUnderstandAttributeName() {
			return MUST_UNDERSTAND_ATTRIBUTE_NAME;
		}

		public QName getMustUnderstandFaultName() {
			return MUST_UNDERSTAND_FAULT_NAME;
		}

		public QName getVersionMismatchFaultName() {
			return VERSION_MISMATCH_FAULT_NAME;
		}

		public String toString() {
			return "SOAP 1.1";
		}
	};

	/**
	 * Represents version 1.2 of the SOAP specification.
	 *
	 * @see <a href="http://www.w3.org/TR/soap12-part0/">SOAP 1.2 specification</a>
	 */
	SoapVersion SOAP_12 = new SoapVersion() {

		private static final String ENVELOPE_NAMESPACE_URI = "http://www.w3.org/2003/05/soap-envelope";

		private static final String NEXT_ROLE_URI = ENVELOPE_NAMESPACE_URI + "/role/next";

		private static final String NONE_ROLE_URI = ENVELOPE_NAMESPACE_URI + "/role/none";

		private static final String ULTIMATE_RECEIVER_ROLE_URI = ENVELOPE_NAMESPACE_URI + "/role/ultimateReceiver";

		private static final String CONTENT_TYPE = "application/soap+xml";

		private QName ENVELOPE_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Envelope");

		private QName HEADER_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Header");

		private QName BODY_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Body");

		private QName FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Fault");

		private QName MUST_UNDERSTAND_ATTRIBUTE_NAME = new QName(ENVELOPE_NAMESPACE_URI, "mustUnderstand");

		private QName ROLE_NAME = new QName(ENVELOPE_NAMESPACE_URI, "role");

		private QName SENDER_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Sender");

		private QName RECEIVER_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "Receiver");

		private QName MUST_UNDERSTAND_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "MustUnderstand");

		private QName VERSION_MISMATCH_FAULT_NAME = new QName(ENVELOPE_NAMESPACE_URI, "VersionMismatch");

		public QName getBodyName() {
			return BODY_NAME;
		}

		public QName getEnvelopeName() {
			return ENVELOPE_NAME;
		}

		public String getEnvelopeNamespaceUri() {
			return ENVELOPE_NAMESPACE_URI;
		}

		public QName getFaultName() {
			return FAULT_NAME;
		}

		public QName getHeaderName() {
			return HEADER_NAME;
		}

		public String getNextActorOrRoleUri() {
			return NEXT_ROLE_URI;
		}

		public String getNoneActorOrRoleUri() {
			return NONE_ROLE_URI;
		}

		public QName getServerOrReceiverFaultName() {
			return RECEIVER_FAULT_NAME;
		}

		public String getUltimateReceiverRoleUri() {
			return ULTIMATE_RECEIVER_ROLE_URI;
		}

		public QName getActorOrRoleName() {
			return ROLE_NAME;
		}

		public QName getClientOrSenderFaultName() {
			return SENDER_FAULT_NAME;
		}

		public String getContentType() {
			return CONTENT_TYPE;
		}

		public QName getMustUnderstandAttributeName() {
			return MUST_UNDERSTAND_ATTRIBUTE_NAME;
		}

		public QName getMustUnderstandFaultName() {
			return MUST_UNDERSTAND_FAULT_NAME;
		}

		public QName getVersionMismatchFaultName() {
			return VERSION_MISMATCH_FAULT_NAME;
		}

		public String toString() {
			return "SOAP 1.2";
		}

	};

	/** Returns the qualified name for a SOAP body. */
	QName getBodyName();

	/** Returns the {@code Content-Type} MIME header for a SOAP message. */
	String getContentType();

	/** Returns the qualified name for a SOAP envelope. */
	QName getEnvelopeName();

	/** Returns the namespace URI for the SOAP envelope namespace. */
	String getEnvelopeNamespaceUri();

	/** Returns the qualified name for a SOAP fault. */
	QName getFaultName();

	/** Returns the qualified name for a SOAP header. */
	QName getHeaderName();

	/** Returns the qualified name of the SOAP {@code MustUnderstand} attribute. */
	QName getMustUnderstandAttributeName();

	/**
	 * Returns the URI indicating that a header element is intended for the next SOAP application that processes the
	 * message.
	 */
	String getNextActorOrRoleUri();

	/** Returns the URI indicating that a header element should never be directly processed. */
	String getNoneActorOrRoleUri();

	/** Returns the qualified name of the {@code MustUnderstand} SOAP Fault value. */
	QName getMustUnderstandFaultName();

	/** Returns the qualified name of the Receiver/Server SOAP Fault value. */
	QName getServerOrReceiverFaultName();

	/** Returns the qualified name of the {@code VersionMismatch} SOAP Fault value. */
	QName getVersionMismatchFaultName();

	/** Returns the qualified name of the SOAP {@code actor}/{@code role} attribute. */
	QName getActorOrRoleName();

	/** Returns the qualified name of the Sender/Client SOAP Fault value. */
	QName getClientOrSenderFaultName();

	/**
	 * Returns the URI indicating that a header element should only be processed by nodes acting as the ultimate
	 * receiver of a message.
	 */
	String getUltimateReceiverRoleUri();
}
