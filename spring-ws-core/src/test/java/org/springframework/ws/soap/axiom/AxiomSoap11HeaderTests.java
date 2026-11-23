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
import org.springframework.ws.soap.soap11.AbstractSoap11HeaderTests;

public class AxiomSoap11HeaderTests extends AbstractSoap11HeaderTests {

	@Override
	protected SoapHeader createSoapHeader() {

		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		AxiomSoapMessage axiomSoapMessage = new AxiomSoapMessage(axiomFactory);

		return axiomSoapMessage.getSoapHeader();
	}

	@Override
	public void testGetResult() {
	}

}
