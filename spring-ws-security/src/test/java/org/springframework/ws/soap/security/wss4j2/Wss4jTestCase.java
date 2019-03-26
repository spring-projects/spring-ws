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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.xpath.Jaxp13XPathTemplate;

import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.Assert.assertTrue;

public abstract class Wss4jTestCase {

	protected MessageFactory saajSoap11MessageFactory;

	protected MessageFactory saajSoap12MessageFactory;

	protected final boolean axiomTest = this.getClass().getSimpleName().startsWith("Axiom");

	protected final boolean saajTest = this.getClass().getSimpleName().startsWith("Saaj");

	protected Jaxp13XPathTemplate xpathTemplate = new Jaxp13XPathTemplate();

	@Before
	public final void setUp() throws Exception {
		if (!axiomTest && !saajTest) {
			throw new IllegalArgumentException("test class name must start with either Axiom or Saaj");
		}
		saajSoap11MessageFactory = MessageFactory.newInstance();
		saajSoap12MessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.put("wsse",
				"https://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
		namespaces.put("ds", "https://www.w3.org/2000/09/xmldsig#");
		namespaces.put("xenc", "https://www.w3.org/2001/04/xmlenc#");
		namespaces.put("wsse11", "https://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd");
		namespaces.put("echo", "http://www.springframework.org/spring-ws/samples/echo");
		namespaces.put("wsu",
				"https://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
		namespaces.put("test", "http://test");
		xpathTemplate.setNamespaces(namespaces);
		onSetup();
	}

	protected void assertXpathEvaluatesTo(String message,
										  String expectedValue,
										  String xpathExpression,
										  Document document) {
		String actualValue = xpathTemplate.evaluateAsString(xpathExpression, new DOMSource(document));
		Assert.assertEquals(message, expectedValue, actualValue);
	}

	protected void assertXpathEvaluatesTo(String message,
										  String expectedValue,
										  String xpathExpression,
										  String document) {
		String actualValue = xpathTemplate.evaluateAsString(xpathExpression, new StringSource(document));
		Assert.assertEquals(message, expectedValue, actualValue);
	}

	protected void assertXpathExists(String message, String xpathExpression, Document document) {
		Node node = xpathTemplate.evaluateAsNode(xpathExpression, new DOMSource(document));
		Assert.assertNotNull(message, node);
	}

	protected void assertXpathNotExists(String message, String xpathExpression, Document document) {
		Node node = xpathTemplate.evaluateAsNode(xpathExpression, new DOMSource(document));
		Assert.assertNull(message, node);
	}

	protected void assertXpathNotExists(String message, String xpathExpression, String document) {
		Node node = xpathTemplate.evaluateAsNode(xpathExpression, new StringSource(document));
		Assert.assertNull(message, node);
	}

	protected SaajSoapMessage loadSaaj11Message(String fileName) throws Exception {
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		Resource resource = new ClassPathResource(fileName, getClass());
		InputStream is = resource.getInputStream();
		try {
			assertTrue("Could not load SAAJ message [" + resource + "]", resource.exists());
			is = resource.getInputStream();
			return new SaajSoapMessage(saajSoap11MessageFactory.createMessage(mimeHeaders, is), saajSoap11MessageFactory);
		}
		finally {
			is.close();
		}
	}
	
	protected SaajSoapMessage loadSaaj12Message(String fileName) throws Exception {
		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "application/soap+xml");
		Resource resource = new ClassPathResource(fileName, getClass());
		InputStream is = resource.getInputStream();
		try {
			assertTrue("Could not load SAAJ message [" + resource + "]", resource.exists());
			is = resource.getInputStream();
			return new SaajSoapMessage(saajSoap12MessageFactory.createMessage(mimeHeaders, is), saajSoap12MessageFactory);
		}
		finally {
			is.close();
		}
	}

	protected AxiomSoapMessage loadAxiom11Message(String fileName) throws Exception {
		Resource resource = new ClassPathResource(fileName, getClass());
		InputStream is = resource.getInputStream();
		try {
			assertTrue("Could not load Axiom message [" + resource + "]", resource.exists());
			is = resource.getInputStream();

			XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(is);
			StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser, null);
			org.apache.axiom.soap.SOAPMessage soapMessage = builder.getSoapMessage();
			return new AxiomSoapMessage(soapMessage, "", true, true);
		}
		finally {
			is.close();
		}
	}

	 @SuppressWarnings("Since15")
	 protected AxiomSoapMessage loadAxiom12Message(String fileName) throws Exception {
		Resource resource = new ClassPathResource(fileName, getClass());
		InputStream is = resource.getInputStream();
		try {
			assertTrue("Could not load Axiom message [" + resource + "]", resource.exists());
			is = resource.getInputStream();

			XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(is);
			StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser, SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
			org.apache.axiom.soap.SOAPMessage soapMessage = builder.getSoapMessage();
			return new AxiomSoapMessage(soapMessage, "", true, true);
		}
		finally {
			is.close();
		}
	}

	protected Object getMessage(SoapMessage soapMessage) {
		if (soapMessage instanceof SaajSoapMessage) {
			return ((SaajSoapMessage) soapMessage).getSaajMessage();
		}
		if (soapMessage instanceof AxiomSoapMessage) {
			return ((AxiomSoapMessage) soapMessage).getAxiomMessage();

		}
		throw new IllegalArgumentException("Illegal message: " + soapMessage);
	}

	protected void setMessage(SoapMessage soapMessage, Object message) {
		if (soapMessage instanceof SaajSoapMessage) {
			((SaajSoapMessage) soapMessage).setSaajMessage((SOAPMessage) message);
			return;
		}
		if (soapMessage instanceof AxiomSoapMessage) {
			((AxiomSoapMessage) soapMessage).setAxiomMessage((org.apache.axiom.soap.SOAPMessage) message);
			return;
		}
		throw new IllegalArgumentException("Illegal message: " + message);
	}

	protected void onSetup() throws Exception {
	}

	protected SoapMessage loadSoap11Message(String fileName) throws Exception {
		if (axiomTest) {
			return loadAxiom11Message(fileName);
		}
		if (saajTest) {
			return loadSaaj11Message(fileName);
		}
		throw new IllegalArgumentException();
	}

	protected SoapMessage loadSoap12Message(String fileName) throws Exception {
		if (axiomTest) {
			return loadAxiom12Message(fileName);
		}
		if (saajTest) {
			return loadSaaj12Message(fileName);
		}
		throw new IllegalArgumentException();
	}

	protected SoapMessageFactory getSoap11MessageFactory() throws Exception {
		if (axiomTest) {
			return new AxiomSoapMessageFactory();
		}
		if (saajTest) {
			return new SaajSoapMessageFactory(saajSoap11MessageFactory);
		}
		throw new IllegalArgumentException();
	}

	protected SoapMessageFactory getSoap12MessageFactory() throws Exception {
		SoapMessageFactory messageFactory;
		if (axiomTest) {
			messageFactory = new AxiomSoapMessageFactory();
		} else if (saajTest) {
			messageFactory = new SaajSoapMessageFactory(saajSoap12MessageFactory);
		} else
			throw new IllegalArgumentException();
		messageFactory.setSoapVersion(SoapVersion.SOAP_12);
		return messageFactory;
	}
	
	protected Document getDocument(SoapMessage message) throws Exception {
		if (axiomTest) {
			return AxiomUtils.toDocument(((AxiomSoapMessage) message).getAxiomMessage().getSOAPEnvelope());
		}
		if (saajTest) {
			return ((SaajSoapMessage) message).getSaajMessage().getSOAPPart();
		}
		throw new IllegalArgumentException();
	}

	protected MessageContext getSoap11MessageContext(final SoapMessage response) throws Exception {
		return new DefaultMessageContext(response, getSoap11MessageFactory()) {
			@Override
			public WebServiceMessage getResponse() {
				return response;
			}
		};
	}

	protected MessageContext getSoap12MessageContext(final SoapMessage response) throws Exception {
		return new DefaultMessageContext(response, getSoap12MessageFactory()) {
			@Override
			public WebServiceMessage getResponse() {
				return response;
			}
		};
	}

}
