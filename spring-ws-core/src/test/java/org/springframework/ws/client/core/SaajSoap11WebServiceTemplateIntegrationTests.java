/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;

import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

/**
 * @author Arjen Poutsma
 */
class SaajSoap11WebServiceTemplateIntegrationTests extends AbstractSoap11WebServiceTemplateIntegrationTests {

	@Override
	protected SoapMessageFactory createMessageFactory() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		return new SaajSoapMessageFactory(messageFactory);
	}

}
