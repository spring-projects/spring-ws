/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.springframework.util.xml.StaxUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.XMLInputFactoryUtils;
import org.springframework.xml.sax.SaxUtils;

@SuppressWarnings("Since15")
public abstract class AbstractEndpointTests {

	protected static final String NAMESPACE_URI = "http://springframework.org/ws";

	protected static final String REQUEST_ELEMENT = "request";

	protected static final String RESPONSE_ELEMENT = "response";

	protected static final String REQUEST = "<" + REQUEST_ELEMENT + " xmlns=\"" + NAMESPACE_URI + "\"/>";

	protected static final String RESPONSE = "<" + RESPONSE_ELEMENT + " xmlns=\"" + NAMESPACE_URI + "\"/>";

	@Test
	void testDomSource() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document requestDocument = documentBuilder.parse(new InputSource(new StringReader(REQUEST)));
		testSource(new DOMSource(requestDocument.getDocumentElement()));
	}

	@Test
	void testSaxSource() throws Exception {
		XMLReader reader = SaxUtils.namespaceAwareXmlReader();
		InputSource inputSource = new InputSource(new StringReader(REQUEST));
		testSource(new SAXSource(reader, inputSource));
	}

	@Test
	void testStaxSourceEventReader() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(REQUEST));
		testSource(new SAXSource(StaxUtils.createXMLReader(eventReader), new InputSource()));
	}

	@Test
	void testStaxSourceStreamReader() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(REQUEST));
		testSource(new SAXSource(StaxUtils.createXMLReader(streamReader), new InputSource()));
	}

	@Test
	void testStreamSourceInputStream() throws Exception {

		InputStream is = new ByteArrayInputStream(REQUEST.getBytes(StandardCharsets.UTF_8));
		testSource(new StreamSource(is));
	}

	@Test
	void testStreamSourceReader() throws Exception {

		Reader reader = new StringReader(REQUEST);
		testSource(new StreamSource(reader));
	}

	protected abstract void testSource(Source requestSource) throws Exception;

}
