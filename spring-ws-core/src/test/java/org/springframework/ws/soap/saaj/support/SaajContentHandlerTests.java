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

import java.util.Iterator;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SaajContentHandlerTests {

	private SaajContentHandler handler;

	private Transformer transformer;

	private SOAPEnvelope envelope;

	@BeforeEach
	void setUp() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage message = messageFactory.createMessage();
		this.envelope = message.getSOAPPart().getEnvelope();
		this.handler = new SaajContentHandler(this.envelope.getBody());
		this.transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	void testHandler() throws Exception {

		String content = "<Root xmlns='http://springframework.org/spring-ws/1' "
				+ "xmlns:child='http://springframework.org/spring-ws/2'>"
				+ "<child:Child attribute='value'>Content</child:Child></Root>";
		Source source = new StringSource(content);
		Result result = new SAXResult(this.handler);
		this.transformer.transform(source, result);
		Name rootName = this.envelope.createName("Root", "", "http://springframework.org/spring-ws/1");
		Iterator<?> iterator = this.envelope.getBody().getChildElements(rootName);

		assertThat(iterator.hasNext()).isTrue();

		SOAPBodyElement rootElement = (SOAPBodyElement) iterator.next();
		Name childName = this.envelope.createName("Child", "child", "http://springframework.org/spring-ws/2");
		iterator = rootElement.getChildElements(childName);

		assertThat(iterator.hasNext()).isTrue();

		SOAPElement childElement = (SOAPElement) iterator.next();

		assertThat(childElement.getValue()).isEqualTo("Content");

		Name attributeName = this.envelope.createName("attribute");

		assertThat(childElement.getAttributeValue(attributeName)).isEqualTo("value");
	}

}
