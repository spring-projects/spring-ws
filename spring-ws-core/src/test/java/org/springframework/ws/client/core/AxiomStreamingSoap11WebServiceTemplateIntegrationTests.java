/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;

/**
 * @author Arjen Poutsma
 */
public class AxiomStreamingSoap11WebServiceTemplateIntegrationTests
		extends AbstractSoap11WebServiceTemplateIntegrationTests {

	@Override
	public SoapMessageFactory createMessageFactory() throws Exception {
		AxiomSoapMessageFactory axiomFactory = new AxiomSoapMessageFactory();
		axiomFactory.setPayloadCaching(false);
		return axiomFactory;
	}

}
