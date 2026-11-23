/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap12.AbstractSoap12MessageTests;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

class SaajSoap12MessageTests extends AbstractSoap12MessageTests {

	@Override
	protected String getNS() {
		return "env";
	}

	@Override
	protected String getHeader() {
		return "";
	}

	private SOAPMessage saajMessage;

	@Override
	protected SoapMessage createSoapMessage() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		this.saajMessage = messageFactory.createMessage();
		this.saajMessage.getSOAPHeader().detachNode();
		return new SaajSoapMessage(this.saajMessage, true, messageFactory);
	}

	@Test
	void getDocumentReflectsAddedHeaderElement() throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SaajSoapMessage message = new SaajSoapMessage(messageFactory.createMessage(), true, messageFactory);
		SoapHeader soapHeader = message.getSoapHeader();
		QName qName = new QName("http://www.w3.org/2005/08/addressing", "Action", "a");
		SoapHeaderElement soapHeaderElement = soapHeader.addHeaderElement(qName);
		soapHeaderElement.setMustUnderstand(true);
		soapHeaderElement.setText("some text");

		XmlAssert xmlAssert = XmlAssert.assertThat(message.getDocument())
			.withNamespaceContext(Map.of("a", "http://www.w3.org/2005/08/addressing"));
		xmlAssert.hasXPath("//a:Action");
		xmlAssert.valueByXPath("//a:Action").isEqualTo("some text");
	}

	@Test
	void testGetPayloadSource() throws Exception {

		this.saajMessage.getSOAPBody().addChildElement("child");
		Source source = this.soapMessage.getPayloadSource();
		StringResult result = new StringResult();
		this.transformer.transform(source, result);

		XmlAssert.assertThat(result.toString()).and("<child/>").ignoreWhitespace().areIdentical();
	}

	@Test
	void testGetPayloadSourceText() throws Exception {

		this.saajMessage.getSOAPBody().addTextNode(" ");
		this.saajMessage.getSOAPBody().addChildElement("child");
		Source source = this.soapMessage.getPayloadSource();
		StringResult result = new StringResult();
		this.transformer.transform(source, result);

		XmlAssert.assertThat(result.toString()).and("<child/>").ignoreWhitespace().areIdentical();
	}

	@Test
	void testGetPayloadResult() throws Exception {

		StringSource source = new StringSource("<child/>");
		Result result = this.soapMessage.getPayloadResult();
		this.transformer.transform(source, result);

		assertThat(this.saajMessage.getSOAPBody().hasChildNodes()).isTrue();
		assertThat(this.saajMessage.getSOAPBody().getFirstChild().getLocalName()).isEqualTo("child");
	}

}
