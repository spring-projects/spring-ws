/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.soap11.AbstractSoap11EnvelopeTests;

public class AxiomSoap11EnvelopeTests extends AbstractSoap11EnvelopeTests {

	@Override
	protected SoapEnvelope createSoapEnvelope() {

		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);

		return axiomSoapMessage.getEnvelope();
	}

}
