/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
