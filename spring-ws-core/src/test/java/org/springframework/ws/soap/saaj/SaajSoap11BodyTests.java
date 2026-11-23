/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.util.Locale;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.soap11.AbstractSoap11BodyTests;

import static org.assertj.core.api.Assertions.assertThat;

class SaajSoap11BodyTests extends AbstractSoap11BodyTests {

	@Override
	protected SoapBody createSoapBody() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage saajMessage = messageFactory.createMessage();

		return new SaajSoap11Body(saajMessage.getSOAPPart().getEnvelope().getBody(), true);
	}

	@Test
	void testLangAttributeOnSoap11FaultString() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		SOAPMessage saajMessage = messageFactory.createMessage();

		SOAPBody saajSoapBody = saajMessage.getSOAPPart().getEnvelope().getBody();
		SaajSoap11Body soapBody = new SaajSoap11Body(saajSoapBody, true);

		soapBody.addClientOrSenderFault("Foo", Locale.ENGLISH);

		assertThat(saajSoapBody.getFault().getFaultStringLocale()).isNotNull();

		saajSoapBody = saajMessage.getSOAPPart().getEnvelope().getBody();
		soapBody = new SaajSoap11Body(saajSoapBody, false);

		soapBody.addClientOrSenderFault("Foo", Locale.ENGLISH);

		assertThat(saajSoapBody.getFault().getFaultStringLocale()).isNull();
	}

}
