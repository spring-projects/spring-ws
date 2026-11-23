/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.soap11;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFaultException;

/**
 * Subinterface of {@code SoapBody} that exposes SOAP 1.1 functionality. Necessary because
 * SOAP 1.1 differs from SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see Soap11Fault
 */
public interface Soap11Body extends SoapBody {

	/**
	 * Adds a SOAP 1.1 {@code Fault} to the body with a localized message. Adding a fault
	 * removes the current content of the body.
	 * @param faultCode the fully qualified fault faultCode
	 * @param faultString the faultString
	 * @param faultStringLocale the faultString locale. May be {@code null}
	 * @return the added {@code Soap11Fault}
	 * @throws IllegalArgumentException if the fault faultCode is not fully qualified
	 */
	Soap11Fault addFault(QName faultCode, String faultString, @Nullable Locale faultStringLocale)
			throws SoapFaultException;

	@Override
	@Nullable Soap11Fault getFault();

	@Override
	Soap11Fault addMustUnderstandFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

	@Override
	Soap11Fault addClientOrSenderFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

	@Override
	Soap11Fault addServerOrReceiverFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

	@Override
	Soap11Fault addVersionMismatchFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

}
