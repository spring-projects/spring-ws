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

package org.springframework.ws;

import static org.xmlunit.assertj.XmlAssert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.xml.StaxUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.XMLInputFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class AbstractWebServiceMessageTestCase {

	protected Transformer transformer;

	protected WebServiceMessage webServiceMessage;

	private Resource payload;

	private String getExpectedString() throws IOException {

		StringWriter expectedWriter = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(payload.getInputStream(), "UTF-8"), expectedWriter);
		return expectedWriter.toString();
	}

	@BeforeEach
	public final void setUp() throws Exception {

		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
		webServiceMessage = createWebServiceMessage();
		payload = new ClassPathResource("payload.xml", AbstractWebServiceMessageTestCase.class);
	}

	@Test
	public void testDomPayload() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document payloadDocument = documentBuilder.parse(SaxUtils.createInputSource(payload));
		DOMSource domSource = new DOMSource(payloadDocument);
		transformer.transform(domSource, webServiceMessage.getPayloadResult());
		Document resultDocument = documentBuilder.newDocument();
		DOMResult domResult = new DOMResult(resultDocument);
		transformer.transform(webServiceMessage.getPayloadSource(), domResult);

		assertThat(resultDocument).and(payloadDocument).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	public void testEventReaderPayload() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(payload.getInputStream());
		Source staxSource = StaxUtils.createCustomStaxSource(eventReader);
		transformer.transform(staxSource, webServiceMessage.getPayloadResult());
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(stringWriter);
		Result staxResult = StaxUtils.createCustomStaxResult(eventWriter);
		transformer.transform(webServiceMessage.getPayloadSource(), staxResult);
		eventWriter.flush();

		assertThat(stringWriter.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	public void testReaderPayload() throws Exception {

		Reader reader = new InputStreamReader(payload.getInputStream(), "UTF-8");
		StreamSource streamSource = new StreamSource(reader, payload.getURL().toString());
		transformer.transform(streamSource, webServiceMessage.getPayloadResult());
		StringWriter resultWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(resultWriter);
		transformer.transform(webServiceMessage.getPayloadSource(), streamResult);

		assertThat(resultWriter.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testSaxPayload() throws Exception {

		SAXSource saxSource = new SAXSource(SaxUtils.createInputSource(payload));
		transformer.transform(saxSource, webServiceMessage.getPayloadResult());
		StringResult stringResult = new StringResult();
		transformer.transform(webServiceMessage.getPayloadSource(), stringResult);

		assertThat(stringResult.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	public void testStreamPayload() throws Exception {

		StreamSource streamSource = new StreamSource(payload.getInputStream(), payload.getURL().toString());
		transformer.transform(streamSource, webServiceMessage.getPayloadResult());
		ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(resultStream);
		transformer.transform(webServiceMessage.getPayloadSource(), streamResult);
		ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
		FileCopyUtils.copy(payload.getInputStream(), expectedStream);

		assertThat(resultStream.toString("UTF-8")).and(expectedStream.toString("UTF-8")).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	public void testStreamReaderPayload() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(payload.getInputStream());
		Source staxSource = StaxUtils.createCustomStaxSource(streamReader);
		transformer.transform(staxSource, webServiceMessage.getPayloadResult());
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(stringWriter);
		Result staxResult = StaxUtils.createCustomStaxResult(streamWriter);
		transformer.transform(webServiceMessage.getPayloadSource(), staxResult);
		streamWriter.flush();

		assertThat(stringWriter.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	private void validateMessage() throws Exception {

		XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		xmlReader.setContentHandler(new DefaultHandler());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		webServiceMessage.writeTo(os);
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		xmlReader.parse(new InputSource(is));
	}

	protected abstract WebServiceMessage createWebServiceMessage() throws Exception;
}
