/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;

import org.springframework.ws.soap.soap12.Soap12Fault;

/**
 * A {@link SaajSoapFault} for SOAP 1.2.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class SaajSoap12Fault extends SaajSoapFault implements Soap12Fault {

	SaajSoap12Fault(SOAPFault fault) {
		super(fault);
	}

	@Override
	public String getFaultActorOrRole() {
		return getSaajFault().getFaultRole();
	}

	@Override
	public void setFaultActorOrRole(String faultRole) {
		try {
			getSaajFault().setFaultRole(faultRole);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<QName> getFaultSubcodes() {
		return getSaajFault().getFaultSubcodes();
	}

	@Override
	public void addFaultSubcode(QName subcode) {
		try {
			getSaajFault().appendFaultSubcode(subcode);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	@Override
	public String getFaultNode() {
		return getSaajFault().getFaultNode();
	}

	@Override
	public void setFaultNode(String uri) {
		try {
			getSaajFault().setFaultNode(uri);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}

	}

	@Override
	public void setFaultReasonText(Locale locale, String text) {
		try {
			getSaajFault().addFaultReasonText(text, locale);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}

	}

	@Override
	public String getFaultReasonText(Locale locale) {
		try {
			return getSaajFault().getFaultReasonText(locale);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

	@Override
	public String getFaultStringOrReason() {
		return getSaajFault().getFaultString();
	}

}
