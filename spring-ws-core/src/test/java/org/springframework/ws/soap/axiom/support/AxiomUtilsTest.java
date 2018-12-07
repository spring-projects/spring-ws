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

package org.springframework.ws.soap.axiom.support;

import java.io.StringWriter;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.custommonkey.xmlunit.XMLAssert.*;

public class AxiomUtilsTest {

	private OMElement element;

	@Before
	public void setUp() throws Exception {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = factory.createOMNamespace("http://www.springframework.org", "prefix");
		element = factory.createOMElement("element", namespace);
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void testToNamespaceDeclared() throws Exception {
		QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
		Assert.assertNotNull("Invalid namespace", namespace);
		Assert.assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
	}

	@Test
	public void testToNamespaceUndeclared() throws Exception {
		QName qName = new QName("http://www.example.com", "localPart");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
		Assert.assertNotNull("Invalid namespace", namespace);
		Assert.assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
		Assert.assertFalse("Invalid prefix", "prefix".equals(namespace.getPrefix()));
	}

	@Test
	public void testToNamespacePrefixDeclared() throws Exception {
		QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart", "prefix");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
		Assert.assertNotNull("Invalid namespace", namespace);
		Assert.assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
		Assert.assertEquals("Invalid prefix", "prefix", namespace.getPrefix());
	}

	@Test
	public void testToNamespacePrefixUndeclared() throws Exception {
		QName qName = new QName("http://www.example.com", "localPart", "otherPrefix");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
		Assert.assertNotNull("Invalid namespace", namespace);
		Assert.assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
		Assert.assertEquals("Invalid prefix", qName.getPrefix(), namespace.getPrefix());
	}

	@Test
	public void testToLanguage() throws Exception {
		Assert.assertEquals("Invalid conversion", "fr-CA", AxiomUtils.toLanguage(Locale.CANADA_FRENCH));
		Assert.assertEquals("Invalid conversion", "en", AxiomUtils.toLanguage(Locale.ENGLISH));
	}

	@Test
	public void testToLocale() throws Exception {
		Assert.assertEquals("Invalid conversion", Locale.CANADA_FRENCH, AxiomUtils.toLocale("fr-CA"));
		Assert.assertEquals("Invalid conversion", Locale.ENGLISH, AxiomUtils.toLocale("en"));
	}

	@Test
	@SuppressWarnings("Since15")
	public void testToDocument() throws Exception {
		Resource resource = new ClassPathResource("org/springframework/ws/soap/soap11/soap11.xml");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));

		SOAPModelBuilder builder = OMXMLBuilderFactory.createSOAPModelBuilder(resource.getInputStream(), null);
		SOAPMessage soapMessage = builder.getSOAPMessage();

		Document result = AxiomUtils.toDocument(soapMessage.getSOAPEnvelope());

		assertXMLEqual("Invalid document generated from SOAPEnvelope", expected, result);
	}

	@Test
	public void testToEnvelope() throws Exception {
		Resource resource = new ClassPathResource("org/springframework/ws/soap/soap11/soap11.xml");

		byte[] buf = FileCopyUtils.copyToByteArray(resource.getFile());
		String expected = new String(buf, "UTF-8");

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(SaxUtils.createInputSource(resource));

		SOAPEnvelope envelope = AxiomUtils.toEnvelope(document);
		StringWriter writer = new StringWriter();
		envelope.serialize(writer);
		String result = writer.toString();

		assertXMLEqual("Invalid SOAPEnvelope generated from document", expected, result);
	}
}
