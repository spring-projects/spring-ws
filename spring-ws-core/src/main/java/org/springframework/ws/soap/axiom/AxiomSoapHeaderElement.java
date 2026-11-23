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
import org.apache.axiom.soap.SOAPHeaderBlock;

import org.springframework.ws.soap.SoapHeaderElement;

/**
 * Axiom-specific version of {@code org.springframework.ws.soap.SoapHeaderHeaderElement}.
 *
 * @author Arjen Poutsma
 */
class AxiomSoapHeaderElement extends AxiomSoapElement implements SoapHeaderElement {

	AxiomSoapHeaderElement(OMElement axiomHeaderBlock, SOAPFactory axiomFactory) {
		super(axiomHeaderBlock, axiomFactory);
	}

	@Override
	public String getActorOrRole() {
		return getAxiomHeaderBlock().getRole();
	}

	@Override
	public void setActorOrRole(String role) {
		getAxiomHeaderBlock().setRole(role);
	}

	@Override
	public boolean getMustUnderstand() {
		return getAxiomHeaderBlock().getMustUnderstand();
	}

	@Override
	public void setMustUnderstand(boolean mustUnderstand) {
		getAxiomHeaderBlock().setMustUnderstand(mustUnderstand);
	}

	@Override
	public Result getResult() {
		try {
			return getAxiomHeaderBlock().getSAXResult();
		}
		catch (OMException ex) {
			throw new AxiomSoapHeaderException(ex);
		}

	}

	@Override
	public String getText() {
		return getAxiomHeaderBlock().getText();
	}

	@Override
	public void setText(String content) {
		getAxiomHeaderBlock().setText(content);
	}

	protected SOAPHeaderBlock getAxiomHeaderBlock() {
		return (SOAPHeaderBlock) getAxiomElement();
	}

}
