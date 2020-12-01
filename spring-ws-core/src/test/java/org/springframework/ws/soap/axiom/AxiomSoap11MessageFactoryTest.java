/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.axiom;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageFactoryTestCase;
import org.springframework.ws.transport.MockTransportInputStream;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.xmlunit.assertj.XmlAssert;

public class AxiomSoap11MessageFactoryTest extends AbstractSoap11MessageFactoryTestCase {

	private Transformer transformer;

	@Override
	protected WebServiceMessageFactory createMessageFactory() throws Exception {

		transformer = TransformerFactoryUtils.newInstance().newTransformer();

		AxiomSoapMessageFactory factory = new AxiomSoapMessageFactory();
		factory.afterPropertiesSet();
		return factory;
	}

	@Override
	public void doTestCreateSoapMessageIllFormedXml() {

		// Axiom parses the contents of XML lazily, so it will not throw an InvalidXmlException when a message is parsed
		throw new InvalidXmlException(null, null);
	}

	@Test
	public void testGetCharsetEncoding() {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();

		assertThat(messageFactory.getCharSetEncoding("text/html; charset=utf-8")).isEqualTo("utf-8");
		assertThat(messageFactory.getCharSetEncoding("application/xop+xml;type=text/xml; charset=utf-8"))
				.isEqualTo("utf-8");
		assertThat(messageFactory.getCharSetEncoding("application/xop+xml;type=\"text/xml; charset=utf-8\""))
				.isEqualTo("utf-8");
	}

	@Test
	public void testRepetitiveReadCaching() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(true);
		messageFactory.afterPropertiesSet();

		String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Body>"
				+ "<root xmlns='http://springframework.org/spring-ws'><child /></root>" + "</soapenv:Body></soapenv:Envelope>";
		TransportInputStream tis = new MockTransportInputStream(new ByteArrayInputStream(xml.getBytes()));
		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		StringResult result = new StringResult();
		transformer.transform(message.getPayloadSource(), result);
		transformer.transform(message.getPayloadSource(), result);
	}

	@Test
	public void testRepetitiveReadNoCaching() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.afterPropertiesSet();

		String xml = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Body>"
				+ "<root xmlns='http://springframework.org/spring-ws'><child /></root>" + "</soapenv:Body></soapenv:Envelope>";
		TransportInputStream tis = new MockTransportInputStream(new ByteArrayInputStream(xml.getBytes()));
		WebServiceMessage message = messageFactory.createWebServiceMessage(tis);

		StringResult result = new StringResult();
		transformer.transform(message.getPayloadSource(), result);

		try {
			transformer.transform(message.getPayloadSource(), result);
			fail("TransformerException expected");
		} catch (TransformerException expected) {
			// ignore
		}
	}

	/**
	 * See http://jira.springframework.org/browse/SWS-502
	 */
	@Test
	public void testSWS502() throws Exception {

		AxiomSoapMessageFactory messageFactory = new AxiomSoapMessageFactory();
		messageFactory.setPayloadCaching(false);
		messageFactory.afterPropertiesSet();

		String envelope = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><soapenv:Body>"
				+ "<ns1:sendMessageResponse xmlns:ns1='urn:Sole' soapenv:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<sendMessageReturn xsi:type='soapenc:string' xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<![CDATA[<?xml version='1.0' encoding='UTF-8'?>" + "<PDresponse>" + "<isStatusOK>true</isStatusOK>"
				+ "<status>0</status>"
				+ "<payLoad><![CDATA[<?xml version='1.0' encoding='UTF-8'?><response>ok</response>]]]]>><![CDATA[</payLoad>"
				+ "</PDresponse>]]></sendMessageReturn>" + "</ns1:sendMessageResponse>" + "</soapenv:Body></soapenv:Envelope>";

		InputStream inputStream = new ByteArrayInputStream(envelope.getBytes("UTF-8"));
		AxiomSoapMessage message = messageFactory.createWebServiceMessage(new MockTransportInputStream(inputStream));

		StringResult result = new StringResult();
		transformer.transform(message.getPayloadSource(), result);

		String expectedPayload = "<ns1:sendMessageResponse xmlns:ns1='urn:Sole' xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' soapenv:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<sendMessageReturn xsi:type='soapenc:string' xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/'>"
				+ "<![CDATA[<?xml version='1.0' encoding='UTF-8'?>" + "<PDresponse>" + "<isStatusOK>true</isStatusOK>"
				+ "<status>0</status>"
				+ "<payLoad><![CDATA[<?xml version='1.0' encoding='UTF-8'?><response>ok</response>]]]]>><![CDATA[</payLoad>"
				+ "</PDresponse>]]></sendMessageReturn>" + "</ns1:sendMessageResponse>";

		XmlAssert.assertThat(result.toString()).and(expectedPayload).ignoreWhitespace().areIdentical();
	}
}
