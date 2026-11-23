/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPProcessingException;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

/**
 * Axiom implementation of {@link SoapFault}.
 *
 * @author Arjen Poutsma
 */
abstract class AxiomSoapFault extends AxiomSoapElement implements SoapFault {

	protected AxiomSoapFault(SOAPFault axiomFault, SOAPFactory axiomFactory) {
		super(axiomFault, axiomFactory);
	}

	@Override
	public @Nullable String getFaultActorOrRole() {
		SOAPFaultRole faultRole = getAxiomFault().getRole();
		return (faultRole != null) ? faultRole.getRoleValue() : null;
	}

	@Override
	public void setFaultActorOrRole(String actor) {
		try {
			SOAPFaultRole axiomFaultRole = getAxiomFactory().createSOAPFaultRole(getAxiomFault());
			axiomFaultRole.setRoleValue(actor);
		}
		catch (SOAPProcessingException ex) {
			throw new AxiomSoapFaultException(ex);
		}

	}

	@Override
	public @Nullable SoapFaultDetail getFaultDetail() {
		try {
			SOAPFaultDetail axiomFaultDetail = getAxiomFault().getDetail();
			return (axiomFaultDetail != null) ? new AxiomSoapFaultDetail(axiomFaultDetail, getAxiomFactory()) : null;
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}

	}

	@Override
	public SoapFaultDetail addFaultDetail() {
		try {
			SOAPFaultDetail axiomFaultDetail = getAxiomFactory().createSOAPFaultDetail(getAxiomFault());
			return new AxiomSoapFaultDetail(axiomFaultDetail, getAxiomFactory());
		}
		catch (OMException ex) {
			throw new AxiomSoapFaultException(ex);
		}

	}

	protected SOAPFault getAxiomFault() {
		return (SOAPFault) getAxiomElement();
	}

}
