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

import org.apache.axiom.om.*;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPModelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringWriter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class AxiomUtilsTest {

	private OMElement element;

	@BeforeEach
	public void setUp() throws Exception {

		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = factory.createOMNamespace("http://www.springframework.org", "prefix");
		element = factory.createOMElement("element", namespace);
	}

	@Test
	public void testToNamespaceDeclared() {

		QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);

		assertThat(namespace).isNotNull();
		assertThat(namespace.getNamespaceURI()).isEqualTo(qName.getNamespaceURI());
	}

	@Test
	public void testToNamespaceUndeclared() {

		QName qName = new QName("http://www.example.com", "localPart");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);

		assertThat(namespace).isNotNull();
		assertThat(namespace.getNamespaceURI()).isEqualTo(qName.getNamespaceURI());
		assertThat(namespace.getPrefix()).isNotEqualTo("prefix");
	}

	@Test
	public void testToNamespacePrefixDeclared() {

		QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart", "prefix");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);

		assertThat(namespace).isNotNull();
		assertThat(namespace.getNamespaceURI()).isEqualTo(qName.getNamespaceURI());
		assertThat(namespace.getPrefix()).isEqualTo("prefix");
	}

	@Test
	public void testToNamespacePrefixUndeclared() {

		QName qName = new QName("http://www.example.com", "localPart", "otherPrefix");
		OMNamespace namespace = AxiomUtils.toNamespace(qName, element);

		assertThat(namespace).isNotNull();
		assertThat(namespace.getNamespaceURI()).isEqualTo(qName.getNamespaceURI());
		assertThat(namespace.getPrefix()).isEqualTo(qName.getPrefix());
	}

	@Test
	public void testToLanguage() {

		assertThat(AxiomUtils.toLanguage(Locale.CANADA_FRENCH)).isEqualTo("fr-CA");
		assertThat(AxiomUtils.toLanguage(Locale.ENGLISH)).isEqualTo("en");
	}

	@Test
	public void testToLocale() {

		assertThat(AxiomUtils.toLocale("fr-CA")).isEqualTo(Locale.CANADA_FRENCH);
		assertThat(AxiomUtils.toLocale("en")).isEqualTo(Locale.ENGLISH);
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

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
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

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}
}
