/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import javax.xml.transform.Result;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFaultDetailElement;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.SoapFaultDetailElement}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomSoapFaultDetailElement extends AxiomSoapElement implements SoapFaultDetailElement {

	AxiomSoapFaultDetailElement(OMElement axiomElement, SOAPFactory soapFactory) {
		super(axiomElement, soapFactory);
	}

	@Override
	public Result getResult() {
		try {
			return getAxiomElement().getSAXResult();
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}

	}

	@Override
	public void addText(@Nullable String text) {
		try {
			getAxiomElement().setText(text);
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}
	}

}
