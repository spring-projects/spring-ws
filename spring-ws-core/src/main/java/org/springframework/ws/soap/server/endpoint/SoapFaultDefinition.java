/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;
import javax.xml.namespace.QName;

/**
 * Defines properties for a SOAP Fault. Used by the {@code SoapFaultDefinitionEditor} and the
 * {@code SoapFaultMappingExceptionResolver}.
 *
 * @author Arjen Poutsma
 * @see SoapFaultDefinitionEditor
 * @see SoapFaultMappingExceptionResolver
 * @since 1.0.0
 */
public class SoapFaultDefinition {

	/**
	 * Constant {@code QName} used to indicate that a {@code Client} fault must be created.
	 *
	 * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String,java.util.Locale)
	 */
	public static final QName CLIENT = new QName("CLIENT");

	/**
	 * Constant {@code QName} used to indicate that a {@code Receiver} fault must be created.
	 *
	 * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String,java.util.Locale)
	 */
	public static final QName RECEIVER = new QName("RECEIVER");

	/**
	 * Constant {@code QName} used to indicate that a {@code Sender} fault must be created.
	 *
	 * @see org.springframework.ws.soap.SoapBody#addServerOrReceiverFault(String,java.util.Locale)
	 */
	public static final QName SENDER = new QName("SENDER");

	/**
	 * Constant {@code QName} used to indicate that a {@code Server}  fault must be created.
	 *
	 * @see org.springframework.ws.soap.SoapBody#addClientOrSenderFault(String,java.util.Locale)
	 */
	public static final QName SERVER = new QName("SERVER");

	private QName faultCode;

	private String faultStringOrReason;

	private Locale locale = Locale.ENGLISH;

	/** Returns the fault code. */
	public QName getFaultCode() {
		return faultCode;
	}

	/** Sets the fault code. */
	public void setFaultCode(QName faultCode) {
		this.faultCode = faultCode;
	}

	/** Returns the fault string or reason text. By default, it is set to the exception message. */
	public String getFaultStringOrReason() {
		return faultStringOrReason;
	}

	/** Sets the fault string or reason text. By default, it is set to the exception message. */
	public void setFaultStringOrReason(String faultStringOrReason) {
		this.faultStringOrReason = faultStringOrReason;
	}

	/**
	 * Gets the fault string locale. By default, it is English.
	 *
	 * @see Locale#ENGLISH
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the fault string locale. By default, it is English.
	 *
	 * @see Locale#ENGLISH
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
}
