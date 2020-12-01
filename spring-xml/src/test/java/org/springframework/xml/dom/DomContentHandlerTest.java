/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.xmlunit.assertj.XmlAssert.*;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DomContentHandlerTest {

	private static final String XML_1 = "<?xml version='1.0' encoding='UTF-8'?>" + "<?pi content?>"
			+ "<root xmlns='namespace'>"
			+ "<prefix:child xmlns:prefix='namespace2' xmlns:prefix2='namespace3' prefix2:attr='value'>content</prefix:child>"
			+ "</root>";

	private static final String XML_2_EXPECTED = "<?xml version='1.0' encoding='UTF-8'?>" + "<root xmlns='namespace'>"
			+ "<child xmlns='namespace2' />" + "</root>";

	private static final String XML_2_SNIPPET = "<?xml version='1.0' encoding='UTF-8'?>" + "<child xmlns='namespace2' />";

	private Document expected;

	private DomContentHandler handler;

	private Document result;

	private XMLReader xmlReader;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	public void setUp() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		result = documentBuilder.newDocument();
		xmlReader = XMLReaderFactory.createXMLReader();
	}

	@Test
	public void testContentHandlerDocumentNamespacePrefixes() throws Exception {

		xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		handler = new DomContentHandler(result);
		expected = documentBuilder.parse(new InputSource(new StringReader(XML_1)));
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(new StringReader(XML_1)));

		assertThat(result).and(expected).areSimilar();
	}

	@Test
	public void testContentHandlerDocumentNoNamespacePrefixes() throws Exception {

		handler = new DomContentHandler(result);
		expected = documentBuilder.parse(new InputSource(new StringReader(XML_1)));
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(new StringReader(XML_1)));

		assertThat(result).and(expected).areSimilar();
	}

	@Test
	public void testContentHandlerElement() throws Exception {

		Element rootElement = result.createElementNS("namespace", "root");
		result.appendChild(rootElement);
		handler = new DomContentHandler(rootElement);
		expected = documentBuilder.parse(new InputSource(new StringReader(XML_2_EXPECTED)));
		xmlReader.setContentHandler(handler);
		xmlReader.parse(new InputSource(new StringReader(XML_2_SNIPPET)));

		assertThat(result).and(expected).areSimilar();
	}
}
