/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;

import org.springframework.util.unit.DataSize;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SaajSoap11MessageFactoryTests extends AbstractSoap11MessageFactoryTests {

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		return new SaajSoapMessageFactory(messageFactory);
	}

	@Test
	void properties() throws IOException {

		Map<String, ?> properties = Collections.singletonMap(SOAPMessage.WRITE_XML_DECLARATION, "true");
		((SaajSoapMessageFactory) this.messageFactory).setMessageProperties(properties);
		SoapMessage soapMessage = (SoapMessage) this.messageFactory.createWebServiceMessage();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		soapMessage.writeTo(os);
		String result = os.toString(StandardCharsets.UTF_8);

		assertThat(result).startsWith("<?xml version=\"1.0\"");
	}

	@Test
	void maxMessageSizeExceededThrowsException() throws Exception {
		SaajSoapMessageFactory factory = new SaajSoapMessageFactory(
				MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
		factory.setMaxMessageSize(DataSize.ofBytes(100));
		StringBuilder sb = new StringBuilder();
		sb.append("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>");
		sb.append("<soapenv:Body><test>");
		for (int i = 0; i < 100; i++) {
			sb.append("<item>AAAAAAAAAAAAAAAAAAAA</item>");
		}
		sb.append("</test></soapenv:Body></soapenv:Envelope>");
		InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
		assertThatExceptionOfType(SoapMessageCreationException.class)
			.isThrownBy(() -> factory.createWebServiceMessage(is))
			.withStackTraceContaining("Maximum message size exceeded: 100B");
	}

	@Test
	void maxGzipDecompressedSizeExceededThrowsException() throws Exception {
		SaajSoapMessageFactory factory = new SaajSoapMessageFactory(
				MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL));
		factory.setMaxMessageSize(DataSize.ofBytes(100));
		StringBuilder sb = new StringBuilder();
		sb.append("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>");
		sb.append("<soapenv:Body><test>");
		for (int i = 0; i < 100; i++) {
			sb.append("<item>AAAAAAAAAAAAAAAAAAAA</item>");
		}
		sb.append("</test></soapenv:Body></soapenv:Envelope>");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
			gzipOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
		}
		InputStream is = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
		assertThatExceptionOfType(SoapMessageCreationException.class)
			.isThrownBy(() -> factory.createWebServiceMessage(is))
			.withStackTraceContaining("Maximum message size exceeded: 100B");
	}

}
