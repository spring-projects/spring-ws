/*
* Copyright 2007 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	   https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.springframework.ws.soap.server.endpoint.annotation;

import javax.xml.namespace.QName;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap11.Soap11Body;

/**
 * Enumeration that represents the standard SOAP Fault codes for use with the JDK 1.5+ {@link SoapFault} annotation.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public enum FaultCode {

	/**
	 * Constant used to indicate that a fault must be created with a custom fault code. When this value is used, the
	 * {@code customFaultCode} string property must be used on {@link SoapFault}.
	 *
	 * <p>Note that custom Fault Codes are only supported on SOAP 1.1.
	 *
	 * @see SoapFault#customFaultCode()
	 * @see Soap11Body#addFault(javax.xml.namespace.QName,String,java.util.Locale)
	 */
	CUSTOM(new QName("CUSTOM")),

	/**
	 * Constant used to indicate that a {@code Client} fault must be created.
	 *
	 * @see SoapBody#addClientOrSenderFault(String,java.util.Locale)
	 */
	CLIENT(new QName("CLIENT")),

	/**
	 * Constant {@code QName} used to indicate that a {@code Receiver} fault must be created.
	 *
	 * @see SoapBody#addServerOrReceiverFault(String,java.util.Locale)
	 */
	RECEIVER(new QName("RECEIVER")),

	/**
	 * Constant {@code QName} used to indicate that a {@code Sender} fault must be created.
	 *
	 * @see SoapBody#addServerOrReceiverFault(String,java.util.Locale)
	 */
	SENDER(new QName("SENDER")),

	/**
	 * Constant {@code QName} used to indicate that a {@code Server}  fault must be created.
	 *
	 * @see SoapBody#addClientOrSenderFault(String,java.util.Locale)
	 */
	SERVER(new QName("SERVER"));

	private final QName value;

	private FaultCode(QName value) {
		this.value = value;
	}

	public QName value() {
		return value;
	}


}
