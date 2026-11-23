/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.util.unit.DataSize;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTests;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatException;

public class AxiomSoap11MessageFactoryTests extends AbstractSoap11MessageFactoryTests {

	private Transformer transformer;

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {

		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();

		AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
		factory.afterPropertiesSet();
		return factory;
	}

	@Override
	public void doTestCreateSoapMessageIllFormedXml() {

		// Axiom parses the contents of XML lazily, so it will not throw an
		// InvalidXmlException when a message is parsed
		throw new InvalidXmlException(null, null);
	}

	@Test
	void testGetCharsetEncoding() {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();

		assertThat(messageFactory.getCharSetEncoding("text/html; charset=utf-8")).isEqualTo("utf-8");
		assertThat(messageFactory.getCharSetEncoding("application/xop+xml;type=text/xml; charset=utf-8"))
			.isEqualTo("utf-8");
		assertThat(messageFactory.getCharSetEncoding("application/xop+xml;type=\"text/xml; charset=utf-8\""))
			.isEqualTo("utf-8");
	}

	@Test
	void testRepetitiveReadCaching() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(true);
		messageFactory.afterPropertiesSet();

		String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Body>"
				+ "<root xmlns='http://springframework.org/spring-ws'><child /></root>"
				+ "</soapenv:Body></soapenv:Envelope>";
		TransportInputStream tis = new MockTransportInputStream(new ByteArrayInputStream(xml.getBytes()));
		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		StringResult result = new StringResult();
		this.transformer.transform(message.getPayloadSource(), result);
		this.transformer.transform(message.getPayloadSource(), result);
	}

	@Test
	void testRepetitiveReadNoCaching() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.afterPropertiesSet();

		String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Body>"
				+ "<root xmlns='http://springframework.org/spring-ws'><child /></root>"
				+ "</soapenv:Body></soapenv:Envelope>";
		TransportInputStream tis = new MockTransportInputStream(new ByteArrayInputStream(xml.getBytes()));
		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		StringResult result = new StringResult();
		this.transformer.transform(message.getPayloadSource(), result);

		assertThatException().isThrownBy(() -> this.transformer.transform(message.getPayloadSource(), result))
			.isInstanceOf(TransformerException.class);
	}

	/**
	 * See http://jira.springframework.org/browse/SWS-502
	 */
	@Test
	void testSWS502() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.afterPropertiesSet();

		String envelope = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><soapenv:Body>"
				+ "<ns1:sendMessageResponse xmlns:ns1='urn:Sole' soapenv:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<sendMessageReturn xsi:type='soapenc:string' xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<![CDATA[<?xml version='1.0' encoding='UTF-8'?>" + "<PDresponse>" + "<isStatusOK>true</isStatusOK>"
				+ "<status>0</status>"
				+ "<payLoad><![CDATA[<?xml version='1.0' encoding='UTF-8'?><response>ok</response>]]]]>><![CDATA[</payLoad>"
				+ "</PDresponse>]]></sendMessageReturn>" + "</ns1:sendMessageResponse>"
				+ "</soapenv:Body></soapenv:Envelope>";

		InputStream inputStream = new ByteArrayInputStream(envelope.getBytes("UTF-8"));
		AxiomSoapMessage message = messageFactory.createWebServiceMessage(new MockTransportInputStream(inputStream));

		StringResult result = new StringResult();
		this.transformer.transform(message.getPayloadSource(), result);

		String expectedPayload = "<ns1:sendMessageResponse xmlns:ns1='urn:Sole' xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' soapenv:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<sendMessageReturn xsi:type='soapenc:string' xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<![CDATA[<?xml version='1.0' encoding='UTF-8'?>" + "<PDresponse>" + "<isStatusOK>true</isStatusOK>"
				+ "<status>0</status>"
				+ "<payLoad><![CDATA[<?xml version='1.0' encoding='UTF-8'?><response>ok</response>]]]]>><![CDATA[</payLoad>"
				+ "</PDresponse>]]></sendMessageReturn>" + "</ns1:sendMessageResponse>";

		XmlAssert.assertThat(result.toString()).and(expectedPayload).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testMaxMessageSizeExceeded() throws Exception {
		AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
		factory.setMaxMessageSize(DataSize.ofBytes(100));
		factory.afterPropertiesSet();
		StringBuilder sb = new StringBuilder();
		sb.append("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>");
		sb.append("<soapenv:Body><test>");
		for (int i = 0; i < 100; i++) {
			sb.append("<item>AAAAAAAAAAAAAAAAAAAA</item>");
		}
		sb.append("</test></soapenv:Body></soapenv:Envelope>");
		TransportInputStream tis = new MockTransportInputStream(
				new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
		assertThatExceptionOfType(AxiomSoapMessageCreationException.class)
			.isThrownBy(() -> factory.createWebServiceMessage(tis))
			.havingCause()
			.withMessageContaining("Maximum message size exceeded");
	}

	@Test
	public void testMaxGzipDecompressedSizeExceeded() throws Exception {
		AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
		factory.setMaxMessageSize(DataSize.ofBytes(100));
		factory.afterPropertiesSet();
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
		TransportInputStream tis = new MockTransportInputStream(
				new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray())));
		assertThatExceptionOfType(AxiomSoapMessageCreationException.class)
			.isThrownBy(() -> factory.createWebServiceMessage(tis))
			.havingCause()
			.withMessageContaining("Maximum message size exceeded");
	}

}
