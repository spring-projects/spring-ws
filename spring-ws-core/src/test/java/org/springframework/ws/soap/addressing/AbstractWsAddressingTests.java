/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing;

import java.io.IOException;
import java.io.InputStream;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.saaj.SaajSoapMessage;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractWsAddressingTests {

	protected MessageFactory messageFactory;

	@BeforeEach
	void createMessageFactory() throws Exception {
		this.messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
	}

	protected SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", " application/soap+xml");
		InputStream is = AbstractWsAddressingTests.class.getResourceAsStream(fileName);

		try (is) {
			assertThat(is).isNotNull();
			return new SaajSoapMessage(this.messageFactory.createMessage(mimeHeaders, is));
		}
	}

	protected void assertXMLSimilar(SaajSoapMessage expected, SaajSoapMessage result) {

		Document expectedDocument = expected.getSaajMessage().getSOAPPart();
		Document resultDocument = result.getSaajMessage().getSOAPPart();

		XmlAssert.assertThat(resultDocument)
			.and(expectedDocument)
			.ignoreWhitespace()
			.ignoreChildNodesOrder()
			.areSimilar();
	}

	protected void assertXMLNotSimilar(SaajSoapMessage expected, SaajSoapMessage result) {

		Document expectedDocument = expected.getSaajMessage().getSOAPPart();
		Document resultDocument = result.getSaajMessage().getSOAPPart();

		XmlAssert.assertThat(resultDocument).and(expectedDocument).ignoreWhitespace().areNotSimilar();
	}

}
