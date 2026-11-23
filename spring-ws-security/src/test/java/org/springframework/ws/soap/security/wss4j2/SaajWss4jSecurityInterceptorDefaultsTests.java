/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.apache.wss4j.dom.handler.RequestData;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SaajWss4jSecurityInterceptorDefaultsTests extends Wss4jTests {

	private static final String PAYLOAD = "<tru:StockSymbol xmlns:tru=\"http://fabrikam123.com/payloads\">QQQ</tru:StockSymbol>";

	@Test
	void testThatTheDefaultValueForAddInclusivePrefixesMatchesWss4JDefaultValue() {

		Wss4jSecurityInterceptor subject = new Wss4jSecurityInterceptor();
		RequestData requestData = new RequestData();
		Boolean springDefault = (Boolean) ReflectionTestUtils.getField(subject, Wss4jSecurityInterceptor.class,
				"addInclusivePrefixes");
		assertThat(requestData.isAddInclusivePrefixes()).isEqualTo(springDefault);
	}

	@Test
	void testThatInitializeValidationRequestDataSetsInclusivePrefixesUsingDefaults()
			throws TransformerException, SOAPException {

		Wss4jSecurityInterceptor subject = new Wss4jSecurityInterceptor();

		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();

		SOAPMessage saajMessage = this.saajSoap11MessageFactory.createMessage();
		transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
		SoapMessage message = new SaajSoapMessage(saajMessage, this.saajSoap11MessageFactory);
		MessageContext messageContext = new DefaultMessageContext(message,
				new SaajSoapMessageFactory(this.saajSoap11MessageFactory));

		RequestData validationData = ReflectionTestUtils.invokeMethod(subject, "initializeValidationRequestData",
				messageContext);

		assertThat(validationData.isAddInclusivePrefixes()).isTrue();
	}

	@Test
	void testThatInitializeValidationRequestDataSetsInclusivePrefixesUsingNotUsingInclusivePrefixes()
			throws TransformerException, SOAPException {

		Wss4jSecurityInterceptor subject = new Wss4jSecurityInterceptor();
		subject.setAddInclusivePrefixes(false);
		Transformer transformer = TransformerFactoryUtils.newInstance().newTransformer();

		SOAPMessage saajMessage = this.saajSoap11MessageFactory.createMessage();
		transformer.transform(new StringSource(PAYLOAD), new DOMResult(saajMessage.getSOAPBody()));
		SoapMessage message = new SaajSoapMessage(saajMessage, this.saajSoap11MessageFactory);
		MessageContext messageContext = new DefaultMessageContext(message,
				new SaajSoapMessageFactory(this.saajSoap11MessageFactory));

		RequestData validationData = ReflectionTestUtils.invokeMethod(subject, "initializeValidationRequestData",
				messageContext);

		assertThat(validationData.isAddInclusivePrefixes()).isFalse();
	}

}
