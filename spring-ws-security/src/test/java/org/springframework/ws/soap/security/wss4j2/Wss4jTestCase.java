/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j2;

import static org.assertj.core.api.Assertions.*;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.xpath.Jaxp13XPathTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class Wss4jTestCase {

	protected MessageFactory saajSoap11MessageFactory;

	protected MessageFactory saajSoap12MessageFactory;

	protected final boolean saajTest = this.getClass().getSimpleName().startsWith("Saaj");

	protected Jaxp13XPathTemplate xpathTemplate = new Jaxp13XPathTemplate();

	@BeforeEach
	public final void setUp() throws Exception {

		if (!saajTest) {
			throw new IllegalArgumentException("test class name must start with Saaj");
		}

		saajSoap11MessageFactory = MessageFactory.newInstance();
		saajSoap12MessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		namespaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
		namespaces.put("xenc", "http://www.w3.org/2001/04/xmlenc#");
		namespaces.put("wsse11", "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd");
		namespaces.put("echo", "http://www.springframework.org/spring-ws/samples/echo");
		namespaces.put("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		namespaces.put("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		namespaces.put("test", "http://test");

		xpathTemplate.setNamespaces(namespaces);

		onSetup();
	}

	protected void assertXpathEvaluatesTo(String message, String expectedValue, String xpathExpression,
			Document document) {

		String actualValue = xpathTemplate.evaluateAsString(xpathExpression, new DOMSource(document));

		assertThat(actualValue).isEqualTo(expectedValue);
	}

	protected void assertXpathEvaluatesTo(String message, String expectedValue, String xpathExpression, String document) {

		String actualValue = xpathTemplate.evaluateAsString(xpathExpression, new StringSource(document));

		assertThat(actualValue).isEqualTo(expectedValue);
	}

	protected void assertXpathExists(String message, String xpathExpression, Document document) {

		Node node = xpathTemplate.evaluateAsNode(xpathExpression, new DOMSource(document));

		assertThat(node).isNotNull();
	}

	protected void assertXpathNotExists(String message, String xpathExpression, Document document) {

		Node node = xpathTemplate.evaluateAsNode(xpathExpression, new DOMSource(document));

		assertThat(node).isNull();
	}

	protected void assertXpathNotExists(String message, String xpathExpression, String document) {

		Node node = xpathTemplate.evaluateAsNode(xpathExpression, new StringSource(document));

		assertThat(node).isNull();
	}

	protected SaajSoapMessage loadSaaj11Message(String fileName) throws Exception {
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		Resource resource = new ClassPathResource(fileName, getClass());

		assertThat(resource.exists()).isTrue();

		try (InputStream is = resource.getInputStream()) {
			return new SaajSoapMessage(saajSoap11MessageFactory.createMessage(mimeHeaders, is), saajSoap11MessageFactory);
		}
	}

	protected SaajSoapMessage loadSaaj12Message(String fileName) throws Exception {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "application/soap+xml");
		Resource resource = new ClassPathResource(fileName, getClass());

		assertThat(resource.exists()).isTrue();

		try (InputStream is = resource.getInputStream()) {
			return new SaajSoapMessage(saajSoap12MessageFactory.createMessage(mimeHeaders, is), saajSoap12MessageFactory);
		}
	}

	protected Object getMessage(SoapMessage soapMessage) {

		if (soapMessage instanceof SaajSoapMessage) {
			return ((SaajSoapMessage) soapMessage).getSaajMessage();
		}

		throw new IllegalArgumentException("Illegal message: " + soapMessage);
	}

	protected void setMessage(SoapMessage soapMessage, Object message) {

		if (soapMessage instanceof SaajSoapMessage) {
			((SaajSoapMessage) soapMessage).setSaajMessage((SOAPMessage) message);
			return;
		}

		throw new IllegalArgumentException("Illegal message: " + message);
	}

	protected void onSetup() throws Exception {}

	protected SoapMessage loadSoap11Message(String fileName) throws Exception {

		if (saajTest) {
			return loadSaaj11Message(fileName);
		}

		throw new IllegalArgumentException();
	}

	protected SoapMessage loadSoap12Message(String fileName) throws Exception {

		if (saajTest) {
			return loadSaaj12Message(fileName);
		}

		throw new IllegalArgumentException();
	}

	protected SoapMessageFactory getSoap11MessageFactory() {

		if (saajTest) {
			return new SaajSoapMessageFactory(saajSoap11MessageFactory);
		}

		throw new IllegalArgumentException();
	}

	protected SoapMessageFactory getSoap12MessageFactory() {

		SoapMessageFactory messageFactory;
		if (saajTest) {
			messageFactory = new SaajSoapMessageFactory(saajSoap12MessageFactory);
		} else
			throw new IllegalArgumentException();
		messageFactory.setSoapVersion(SoapVersion.SOAP_12);
		return messageFactory;
	}

	protected Document getDocument(SoapMessage message) {

		if (saajTest) {
			return ((SaajSoapMessage) message).getSaajMessage().getSOAPPart();
		}
		throw new IllegalArgumentException();
	}

	protected MessageContext getSoap11MessageContext(final SoapMessage response) {

		return new DefaultMessageContext(response, getSoap11MessageFactory()) {

			@Override
			public WebServiceMessage getResponse() {
				return response;
			}
		};
	}

	protected MessageContext getSoap12MessageContext(final SoapMessage response) {

		return new DefaultMessageContext(response, getSoap12MessageFactory()) {

			@Override
			public WebServiceMessage getResponse() {
				return response;
			}
		};
	}
}
