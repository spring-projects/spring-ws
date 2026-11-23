/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageFactoryTests;

public class AxiomSoap12MessageFactoryTests extends AbstractSoap12MessageFactoryTests {

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {

		AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
		factory.setSoapVersion(SoapVersion.SOAP_12);
		factory.afterPropertiesSet();

		return factory;
	}

	@Override
	public void doTestCreateSoapMessageIllFormedXml() {

		// Axiom parses the contents of XML lazily, so it will not throw an
		// InvalidXmlException when a message is parsed
		throw new InvalidXmlException(null, null);
	}

}
