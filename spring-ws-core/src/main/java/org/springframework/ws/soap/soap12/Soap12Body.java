/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.soap12;

import java.util.Locale;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFaultException;

/**
 * Subinterface of {@code SoapBody} that exposes SOAP 1.2 functionality. Necessary because
 * SOAP 1.1 differs from SOAP 1.2 with respect to SOAP Faults.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface Soap12Body extends SoapBody {

	/**
	 * Adds a {@code DataEncodingUnknown} fault to the body.
	 * <p>
	 * Adding a fault removes the current content of the body.
	 * @param subcodes the optional fully qualified fault subcodes
	 * @param reason the fault reason
	 * @param locale the language of the fault reason
	 * @return the created {@code SoapFault}
	 */
	Soap12Fault addDataEncodingUnknownFault(QName[] subcodes, String reason, Locale locale) throws SoapFaultException;

	@Override
	@Nullable Soap12Fault getFault();

	@Override
	Soap12Fault addMustUnderstandFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

	@Override
	Soap12Fault addClientOrSenderFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

	@Override
	Soap12Fault addServerOrReceiverFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

	@Override
	Soap12Fault addVersionMismatchFault(String faultStringOrReason, @Nullable Locale locale) throws SoapFaultException;

}
