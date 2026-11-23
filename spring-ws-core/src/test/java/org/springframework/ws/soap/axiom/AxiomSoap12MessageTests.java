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
import org.springframework.ws.soap.soap12.AbstractSoap12MessageTests;

public class AxiomSoap12MessageTests extends AbstractSoap12MessageTests {

	@Override
	protected String getNS() {
		return "soapenv";
	}

	@Override
	protected SoapMessage createSoapMessage() {

		SOAPFactory axiomFactory = OMAbstractFactory.getSOAP12Factory();
		return new AxiomSoapMessage(axiomFactory);
	}

}
