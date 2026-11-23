/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageTests;

public class AxiomSoap11MessageTests extends AbstractSoap11MessageTests {

	@Override
	protected String getNS() {
		return "soapenv";
	}

	@Override
	protected SoapMessage createSoapMessage() {

		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP11Factory();
		return new AxiomSoapMessage(axiomFactory);
	}

}
