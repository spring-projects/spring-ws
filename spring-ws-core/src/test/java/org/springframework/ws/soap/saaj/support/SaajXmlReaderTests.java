/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.soap.saaj.support;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class SaajXmlReaderTests {

	private SaajXmlReader saajReader;

	private SOAPMessage message;

	private Transformer transformer;

	@BeforeEach
	public void setUp() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		this.message = messageFactory.createMessage();
		SOAPEnvelope envelope = this.message.getSOAPPart().getEnvelope();
		this.saajReader = new SaajXmlReader(envelope);
		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	public void testNamespacesPrefixes() throws Exception {

		this.saajReader.setFeature("http://xml.org/sax/features/namespaces", true);
		this.saajReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		DOMResult result = new DOMResult();
		Source source = new SAXSource(this.saajReader, new InputSource());
		this.transformer.transform(source, result);
		DOMResult expected = new DOMResult();
		this.transformer.transform(new DOMSource(this.message.getSOAPPart().getEnvelope()), expected);

		assertThat(result.getNode()).and(expected.getNode()).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testNamespacesNoPrefixes() throws Exception {

		this.saajReader.setFeature("http://xml.org/sax/features/namespaces", true);
		this.saajReader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
		DOMResult result = new DOMResult();
		Source source = new SAXSource(this.saajReader, new InputSource());
		this.transformer.transform(source, result);
		DOMResult expected = new DOMResult();
		this.transformer.transform(new DOMSource(this.message.getSOAPPart().getEnvelope()), expected);

		assertThat(result.getNode()).and(expected.getNode()).ignoreWhitespace().areIdentical();
	}

}
