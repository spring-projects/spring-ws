/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
