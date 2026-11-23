/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import javax.xml.namespace.QName;

import jakarta.xml.soap.Detail;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

/**
 * SAAJ-specific abstract base class of the {@code SoapFault} interface. Wraps a
 * {@link jakarta.xml.soap.SOAPFault}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajSoapFault extends SaajSoapElement<SOAPFault> implements SoapFault {

	protected SaajSoapFault(SOAPFault fault) {
		super(fault);
	}

	@Override
	public QName getFaultCode() {
		return getSaajFault().getFaultCodeAsQName();
	}

	protected SOAPFault getSaajFault() {
		return getSaajElement();
	}

	@Override
	public @Nullable SoapFaultDetail getFaultDetail() {
		Detail saajDetail = getSaajFault().getDetail();
		return (saajDetail != null) ? new SaajSoapFaultDetail(saajDetail) : null;
	}

	@Override
	public SoapFaultDetail addFaultDetail() {
		try {
			Detail saajDetail = getSaajFault().addDetail();
			return new SaajSoapFaultDetail(saajDetail);
		}
		catch (SOAPException ex) {
			throw new SaajSoapFaultException(ex);
		}
	}

}
