/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.util.Locale;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;

import org.springframework.ws.soap.soap11.Soap11Fault;

/**
 * SAAJ-specific implementation of the {@code Soap11Fault} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPFault}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoap11Fault extends SaajSoapFault implements Soap11Fault {

	SaajSoap11Fault(SOAPFault fault) {
		super(fault);
	}

	@Override
	public String getFaultActorOrRole() {
		return getSaajFault().getFaultActor();
	}

	@Override
	public void setFaultActorOrRole(String faultActor) {
		try {
			getSaajFault().setFaultActor(faultActor);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	@Override
	public String getFaultStringOrReason() {
		return getSaajFault().getFaultString();
	}

	@Override
	public Locale getFaultStringLocale() {
		return getSaajFault().getFaultStringLocale();
	}

}
