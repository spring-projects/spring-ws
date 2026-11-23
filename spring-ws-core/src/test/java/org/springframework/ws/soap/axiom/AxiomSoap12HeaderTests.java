/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.soap12.AbstractSoap12HeaderTests;

public class AxiomSoap12HeaderTests extends AbstractSoap12HeaderTests {

	@Override
	protected SoapHeader createSoapHeader() {

		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP12Factory();
		AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);

		return axiomSoapMessage.getSoapHeader();
	}

	@Override
	public void testGetResult() {
	}

}
