/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;

import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.soap11.AbstractSoap11EnvelopeTests;

class SaajSoap11EnvelopeTests extends AbstractSoap11EnvelopeTests {

	@Override
	protected SoapEnvelope createSoapEnvelope() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage saajMessage = messageFactory.createMessage();

		return new SaajSoapEnvelope(saajMessage.getSOAPPart().getEnvelope(), true);
	}

}
