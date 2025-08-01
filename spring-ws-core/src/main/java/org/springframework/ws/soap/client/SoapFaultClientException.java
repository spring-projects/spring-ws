/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.soap.client;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.client.WebServiceFaultException;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;

/**
 * Thrown by {@code SoapFaultMessageResolver} when the response message has a fault.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapFaultClientException extends WebServiceFaultException {

	private final @Nullable SoapFault soapFault;

	/**
	 * Create a new instance of the {@code SoapFaultClientException} class.
	 * @param faultMessage the fault message
	 */
	public SoapFaultClientException(SoapMessage faultMessage) {
		super(faultMessage);
		SoapBody body = faultMessage.getSoapBody();
		this.soapFault = body.getFault();
	}

	/** Returns the {@link SoapFault}. */
	public @Nullable SoapFault getSoapFault() {
		return this.soapFault;
	}

	/** Returns the fault code. */
	public @Nullable QName getFaultCode() {
		return (this.soapFault != null) ? this.soapFault.getFaultCode() : null;
	}

	/**
	 * Returns the fault string or reason. For SOAP 1.1, this returns the fault string.
	 * For SOAP 1.2, this returns the fault reason for the default locale.
	 * <p>
	 * Note that this message returns the same as {@link #getMessage()}.
	 */
	public @Nullable String getFaultStringOrReason() {
		return (this.soapFault != null) ? this.soapFault.getFaultStringOrReason() : null;
	}

}
