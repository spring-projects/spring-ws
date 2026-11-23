/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;

/**
 * @author Arjen Poutsma
 */
public class AxiomNonStreamingSoap12WebServiceTemplateIntegrationTests
		extends AbstractSoap12WebServiceTemplateIntegrationTests {

	@Override
	public SoapMessageFactory createMessageFactory() {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setSoapVersion(SoapVersion.SOAP_12);
		return messageFactory;
	}

}
