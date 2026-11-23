/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTests;

public class AxiomSoap11NonCachingBodyTests extends AbstractSoap11BodyTests {

	@Override
	protected SoapBody createSoapBody() {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.setSoapVersion(SoapVersion.SOAP_11);

		AxiomSoapMessage axiomSoapMessage = messageFactory.createWebServiceMessage();
		return axiomSoapMessage.getSoapBody();
	}

}
