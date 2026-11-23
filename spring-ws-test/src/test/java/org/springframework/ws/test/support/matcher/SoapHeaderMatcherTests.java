/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.matcher;

import javax.xml.namespace.QName;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.easymock.EasyMock.createMock;

class SoapHeaderMatcherTests {

	private SoapHeaderMatcher matcher;

	private QName expectedHeaderName;

	@BeforeEach
	void setUp() {

		this.expectedHeaderName = new QName("http://example.com", "header");
		this.matcher = new SoapHeaderMatcher(this.expectedHeaderName);
	}

	@Test
	void match() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage saajMessage = messageFactory.createMessage();
		saajMessage.getSOAPHeader().addHeaderElement(this.expectedHeaderName);
		SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

		this.matcher.match(soapMessage);
	}

	@Test
	void nonMatch() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage saajMessage = messageFactory.createMessage();
			SoapMessage soapMessage = new SaajSoapMessage(saajMessage);

			this.matcher.match(soapMessage);
		});
	}

	@Test
	void nonSoap() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			WebServiceMessage message = createMock(WebServiceMessage.class);

			this.matcher.match(message);
		});
	}

}
