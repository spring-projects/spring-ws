/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageFactoryTests;

class SaajSoap12MessageFactoryTests extends AbstractSoap12MessageFactoryTests {

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		return new SaajSoapMessageFactory(messageFactory);
	}

}
