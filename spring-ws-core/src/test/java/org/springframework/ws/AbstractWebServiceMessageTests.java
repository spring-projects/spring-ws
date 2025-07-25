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

package org.springframework.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.xml.StaxUtils;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.XMLInputFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public abstract class AbstractWebServiceMessageTests {

	protected Transformer transformer;

	protected WebServiceMessage webServiceMessage;

	private Resource payload;

	private String getExpectedString() throws IOException {

		StringWriter expectedWriter = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(this.payload.getInputStream(), StandardCharsets.UTF_8),
				expectedWriter);
		return expectedWriter.toString();
	}

	@BeforeEach
	public final void setUp() throws Exception {

		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		this.transformer = transformerFactory.newTransformer();
		this.webServiceMessage = createWebServiceMessage();
		this.payload = new ClassPathResource("payload.xml", AbstractWebServiceMessageTests.class);
	}

	@Test
	void testDomPayload() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document payloadDocument = documentBuilder.parse(SaxUtils.createInputSource(this.payload));
		DOMSource domSource = new DOMSource(payloadDocument);
		this.transformer.transform(domSource, this.webServiceMessage.getPayloadResult());
		Document resultDocument = documentBuilder.newDocument();
		DOMResult domResult = new DOMResult(resultDocument);
		this.transformer.transform(this.webServiceMessage.getPayloadSource(), domResult);

		assertThat(resultDocument).and(payloadDocument).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	void testEventReaderPayload() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(this.payload.getInputStream());
		Source staxSource = StaxUtils.createCustomStaxSource(eventReader);
		this.transformer.transform(staxSource, this.webServiceMessage.getPayloadResult());
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(stringWriter);
		Result staxResult = StaxUtils.createCustomStaxResult(eventWriter);
		this.transformer.transform(this.webServiceMessage.getPayloadSource(), staxResult);
		eventWriter.flush();

		assertThat(stringWriter.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	void testReaderPayload() throws Exception {

		Reader reader = new InputStreamReader(this.payload.getInputStream(), StandardCharsets.UTF_8);
		StreamSource streamSource = new StreamSource(reader, this.payload.getURL().toString());
		this.transformer.transform(streamSource, this.webServiceMessage.getPayloadResult());
		StringWriter resultWriter = new StringWriter();
		StreamResult streamResult = new StreamResult(resultWriter);
		this.transformer.transform(this.webServiceMessage.getPayloadSource(), streamResult);

		assertThat(resultWriter.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();
	}

	@Test
	void testSaxPayload() throws Exception {

		SAXSource saxSource = new SAXSource(SaxUtils.createInputSource(this.payload));
		this.transformer.transform(saxSource, this.webServiceMessage.getPayloadResult());
		StringResult stringResult = new StringResult();
		this.transformer.transform(this.webServiceMessage.getPayloadSource(), stringResult);

		assertThat(stringResult.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	@Test
	void testStreamPayload() throws Exception {

		StreamSource streamSource = new StreamSource(this.payload.getInputStream(), this.payload.getURL().toString());
		this.transformer.transform(streamSource, this.webServiceMessage.getPayloadResult());
		ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(resultStream);
		this.transformer.transform(this.webServiceMessage.getPayloadSource(), streamResult);
		ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
		FileCopyUtils.copy(this.payload.getInputStream(), expectedStream);

		assertThat(resultStream.toString(StandardCharsets.UTF_8)).and(expectedStream.toString(StandardCharsets.UTF_8))
			.ignoreWhitespace()
			.areIdentical();

		validateMessage();
	}

	@Test
	void testStreamReaderPayload() throws Exception {

		XMLInputFactory inputFactory = XMLInputFactoryUtils.newInstance();
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(this.payload.getInputStream());
		Source staxSource = StaxUtils.createCustomStaxSource(streamReader);
		this.transformer.transform(staxSource, this.webServiceMessage.getPayloadResult());
		StringWriter stringWriter = new StringWriter();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(stringWriter);
		Result staxResult = StaxUtils.createCustomStaxResult(streamWriter);
		this.transformer.transform(this.webServiceMessage.getPayloadSource(), staxResult);
		streamWriter.flush();

		assertThat(stringWriter.toString()).and(getExpectedString()).ignoreWhitespace().areIdentical();

		validateMessage();
	}

	private void validateMessage() throws Exception {
		XMLReader xmlReader = SaxUtils.namespaceAwareXmlReader();
		xmlReader.setContentHandler(new DefaultHandler());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		this.webServiceMessage.writeTo(os);
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		xmlReader.parse(new InputSource(is));
	}

	protected abstract WebServiceMessage createWebServiceMessage() throws Exception;

}
