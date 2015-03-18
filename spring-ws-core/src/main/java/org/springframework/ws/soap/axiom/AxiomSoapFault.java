/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPProcessingException;

import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;

/** @author Arjen Poutsma */
abstract class AxiomSoapFault extends AxiomSoapElement implements SoapFault {

	protected AxiomSoapFault(SOAPFault axiomFault, SOAPFactory axiomFactory) {
		super(axiomFault, axiomFactory);
	}

	@Override
	public String getFaultActorOrRole() {
		SOAPFaultRole faultRole = getAxiomFault().getRole();
		return faultRole != null ? faultRole.getRoleValue() : null;
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
	public SoapFaultDetail getFaultDetail() {
		try {
			SOAPFaultDetail axiomFaultDetail = getAxiomFault().getDetail();
			return axiomFaultDetail != null ? new AxiomSoapFaultDetail(axiomFaultDetail, getAxiomFactory()) : null;
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
