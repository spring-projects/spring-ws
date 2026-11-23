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
public class AxiomStreamingSoap12WebServiceTemplateIntegrationTests
		extends AbstractSoap12WebServiceTemplateIntegrationTests {

	@Override
	public SoapMessageFactory createMessageFactory() throws Exception {
		AxiomSoapMessageFactory axiomFactory = new AxiomSoapMessageFactory();
		axiomFactory.setSoapVersion(SoapVersion.SOAP_12);
		axiomFactory.setPayloadCaching(false);
		return axiomFactory;
	}

}
