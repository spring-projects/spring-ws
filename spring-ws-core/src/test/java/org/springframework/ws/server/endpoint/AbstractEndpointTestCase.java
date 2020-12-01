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

package org.springframework.ws.server.endpoint;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

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
import org.springframework.util.xml.StaxUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.XMLInputFactoryUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

@SuppressWarnings("Since15")
public abstract class AbstractEndpointTestCase {

	protected static final String NAMESPACE_URI = "http://springframework.org/ws";

	protected static final String REQUEST_ELEMENT = "request";

	protected static final String RESPONSE_ELEMENT = "response";

	protected static final String REQUEST = "<" + REQUEST_ELEMENT + " xmlns=\"" + NAMESPACE_URI + "\"/>";

	protected static final String RESPONSE = "<" + RESPONSE_ELEMENT + " xmlns=\"" + NAMESPACE_URI + "\"/>";

	@Test
	public void testDomSource() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document requestDocument = documentBuilder.parse(new InputSource(new StringReader(REQUEST)));
		testSource(new DOMSource(requestDocument.getDocumentElement()));
	}

	@Test
	public void testSaxSource() throws Exception {

		XMLReader reader = XMLReaderFactory.createXMLReader();
		InputSource inputSource = new InputSource(new StringReader(REQUEST));
		testSource(new SAXSource(reader, inputSource));
	}

	@Test
	public void testStaxSourceEventReader() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(REQUEST));
		testSource(new SAXSource(StaxUtils.createXMLReader(eventReader), new InputSource()));
	}

	@Test
	public void testStaxSourceStreamReader() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(REQUEST));
		testSource(new SAXSource(StaxUtils.createXMLReader(streamReader), new InputSource()));
	}

	@Test
	public void testStreamSourceInputStream() throws Exception {

		InputStream is = new ByteArrayInputStream(REQUEST.getBytes("UTF-8"));
		testSource(new StreamSource(is));
	}

	@Test
	public void testStreamSourceReader() throws Exception {

		Reader reader = new StringReader(REQUEST);
		testSource(new StreamSource(reader));
	}

	protected abstract void testSource(Source requestSource) throws Exception;
}
