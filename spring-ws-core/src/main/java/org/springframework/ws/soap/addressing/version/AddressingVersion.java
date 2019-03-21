/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.addressing.version;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;

/**
 * Defines the contract for a specific version of the WS-Addressing specification.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface AddressingVersion {

	/**
	 * Returns the {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties} for the given
	 * message.
	 *
	 * @param message the message to find the map for
	 * @return the message addressing properties
	 * @see <a href="http://www.w3.org/TR/ws-addr-core/#msgaddrprops">Message Addressing Properties</a>
	 */
	MessageAddressingProperties getMessageAddressingProperties(SoapMessage message);

	/**
	 * Adds addressing SOAP headers to the given message, using the given {@link MessageAddressingProperties}.
	 *
	 * @param message the message to add the headers to
	 * @param map	  the message addressing properties
	 */
	void addAddressingHeaders(SoapMessage message, MessageAddressingProperties map);

	/**
	 * Given a {@code SoapHeaderElement}, return whether or not this version understands it.
	 *
	 * @param headerElement the header
	 * @return {@code true} if understood, {@code false} otherwise
	 */
	boolean understands(SoapHeaderElement headerElement);

	/**
	 * Indicates whether the given {@link MessageAddressingProperties} has all required properties.
	 *
	 * @return {@code true} if the to and action properties have been set, and - if a reply or fault endpoint has
	 *		   been set - also checks for the message id
	 */
	boolean hasRequiredProperties(MessageAddressingProperties map);

	/*
	* Address URIs
	*/

	/**
	 * Indicates whether the given endpoint reference has a Anonymous address. This address is used to indicate that a
	 * message should be sent in-band.
	 *
	 * @see <a href="http://www.w3.org/TR/ws-addr-core/#formreplymsg">Formulating a Reply Message</a>
	 */
	boolean hasAnonymousAddress(EndpointReference epr);

	/**
	 * Indicates whether the given endpoint reference has a None address. Messages to be sent to this address will not
	 * be sent.
	 *
	 * @see <a href="http://www.w3.org/TR/ws-addr-core/#sendmsgepr">Sending a Message to an EPR</a>
	 */
	boolean hasNoneAddress(EndpointReference epr);

	/*
	 * Faults
	 */

	/**
	 * Adds a Invalid Addressing Header fault to the given message.
	 *
	 * @see <a href="http://www.w3.org/TR/ws-addr-soap/#invalidmapfault">Invalid Addressing Header</a>
	 */
	SoapFault addInvalidAddressingHeaderFault(SoapMessage message);

	/**
	 * Adds a Message Addressing Header Required fault to the given message.
	 *
	 * @see <a href="http://www.w3.org/TR/ws-addr-soap/#missingmapfault">Message Addressing Header Required</a>
	 */
	SoapFault addMessageAddressingHeaderRequiredFault(SoapMessage message);

}
