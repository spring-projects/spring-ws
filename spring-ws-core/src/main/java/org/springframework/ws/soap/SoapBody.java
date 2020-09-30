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

import java.util.Locale;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.WebServiceMessage;

/**
 * Represents the {@code Body} element in a SOAP message. A SOAP body contains the <strong>payload</strong> of the
 * message. This payload can be custom XML, or a {@code SoapFault} (but not both).
 * <p>
 * Note that the source returned by {@code getSource()} includes the SOAP Body element itself. For the contents of the
 * body, use {@code getPayloadSource()}.
 *
 * @author Arjen Poutsma
 * @see SoapEnvelope#getBody()
 * @see #getPayloadSource()
 * @see #getPayloadResult()
 * @see SoapFault
 * @since 1.0.0
 */
public interface SoapBody extends SoapElement {

	/**
	 * Returns a {@code Source} that represents the contents of the body.
	 *
	 * @return the message contents
	 * @see WebServiceMessage#getPayloadSource()
	 */
	Source getPayloadSource();

	/**
	 * Returns a {@code Result} that represents the contents of the body.
	 * <p>
	 * Calling this method removes the current content of the body.
	 *
	 * @return the message contents
	 * @see WebServiceMessage#getPayloadResult()
	 */
	Result getPayloadResult();

	/**
	 * Adds a {@code MustUnderstand} fault to the body. A {@code MustUnderstand} is returned when a SOAP header with a
	 * {@code MustUnderstand} attribute is not understood.
	 * <p>
	 * Adding a fault removes the current content of the body.
	 *
	 * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
	 * @param locale the language of faultStringOrReason. Optional for SOAP 1.1
	 * @return the created {@code SoapFault}
	 */
	SoapFault addMustUnderstandFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

	/**
	 * Adds a {@code Client}/{@code Sender} fault to the body. For SOAP 1.1, this adds a fault with a {@code Client} fault
	 * code. For SOAP 1.2, this adds a fault with a {@code Sender} code.
	 * <p>
	 * Adding a fault removes the current content of the body.
	 *
	 * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
	 * @param locale the language of faultStringOrReason. Optional for SOAP 1.1
	 * @return the created {@code SoapFault}
	 */
	SoapFault addClientOrSenderFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

	/**
	 * Adds a {@code Server}/{@code Receiver} fault to the body. For SOAP 1.1, this adds a fault with a {@code Server}
	 * fault code. For SOAP 1.2, this adds a fault with a {@code Receiver} code.
	 * <p>
	 * Adding a fault removes the current content of the body.
	 *
	 * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
	 * @param locale the language of faultStringOrReason. Optional for SOAP 1.1
	 * @return the created {@code SoapFault}
	 */
	SoapFault addServerOrReceiverFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

	/**
	 * Adds a {@code VersionMismatch} fault to the body.
	 * <p>
	 * Adding a fault removes the current content of the body.
	 *
	 * @param faultStringOrReason the SOAP 1.1 fault string or SOAP 1.2 reason text
	 * @param locale the language of faultStringOrReason. Optional for SOAP 1.1
	 * @return the created {@code SoapFault}
	 */
	SoapFault addVersionMismatchFault(String faultStringOrReason, Locale locale) throws SoapFaultException;

	/**
	 * Indicates whether this body has a {@code SoapFault}.
	 *
	 * @return {@code true} if the body has a fault; {@code false} otherwise
	 */
	boolean hasFault();

	/**
	 * Returns the {@code SoapFault} of this body.
	 *
	 * @return the {@code SoapFault}, or {@code null} if none is present
	 */
	SoapFault getFault();
}
