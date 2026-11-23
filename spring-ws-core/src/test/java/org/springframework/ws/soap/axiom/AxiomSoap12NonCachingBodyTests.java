/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.AbstractSoap12BodyTests;

public class AxiomSoap12NonCachingBodyTests extends AbstractSoap12BodyTests {

	@Override
	protected SoapBody createSoapBody() {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.setSoapVersion(SoapVersion.SOAP_12);

		AxiomSoapMessage axiomSoapMessage = messageFactory.createWebServiceMessage();

		return axiomSoapMessage.getSoapBody();
	}

}
