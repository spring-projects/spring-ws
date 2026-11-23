/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.dom;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;

import static org.xmlunit.assertj.XmlAssert.assertThat;

class DomContentHandlerTests {

	private static final String XML_1 = "<?xml version='1.0' encoding='UTF-8'?>" + "<?pi content?>"
			+ "<root xmlns='namespace'>"
			+ "<prefix:child xmlns:prefix='namespace2' xmlns:prefix2='namespace3' prefix2:attr='value'>content</prefix:child>"
			+ "</root>";

	private static final String XML_2_EXPECTED = "<?xml version='1.0' encoding='UTF-8'?>" + "<root xmlns='namespace'>"
			+ "<child xmlns='namespace2' />" + "</root>";

	private static final String XML_2_SNIPPET = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<child xmlns='namespace2' />";

	private Document expected;

	private DomContentHandler handler;

	private Document result;

	private XMLReader xmlReader;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	void setUp() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
		this.result = this.documentBuilder.newDocument();
		this.xmlReader = SaxUtils.namespaceAwareXmlReader();
	}

	@Test
	void testContentHandlerDocumentNamespacePrefixes() throws Exception {

		this.xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		this.handler = new DomContentHandler(this.result);
		this.expected = this.documentBuilder.parse(new InputSource(new StringReader(XML_1)));
		this.xmlReader.setContentHandler(this.handler);
		this.xmlReader.parse(new InputSource(new StringReader(XML_1)));

		assertThat(this.result).and(this.expected).areSimilar();
	}

	@Test
	void testContentHandlerDocumentNoNamespacePrefixes() throws Exception {

		this.handler = new DomContentHandler(this.result);
		this.expected = this.documentBuilder.parse(new InputSource(new StringReader(XML_1)));
		this.xmlReader.setContentHandler(this.handler);
		this.xmlReader.parse(new InputSource(new StringReader(XML_1)));

		assertThat(this.result).and(this.expected).areSimilar();
	}

	@Test
	void testContentHandlerElement() throws Exception {

		Element rootElement = this.result.createElementNS("namespace", "root");
		this.result.appendChild(rootElement);
		this.handler = new DomContentHandler(rootElement);
		this.expected = this.documentBuilder.parse(new InputSource(new StringReader(XML_2_EXPECTED)));
		this.xmlReader.setContentHandler(this.handler);
		this.xmlReader.parse(new InputSource(new StringReader(XML_2_SNIPPET)));

		assertThat(this.result).and(this.expected).areSimilar();
	}

}
