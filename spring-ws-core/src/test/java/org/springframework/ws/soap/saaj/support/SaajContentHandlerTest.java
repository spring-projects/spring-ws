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

package org.springframework.ws.soap.saaj.support;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

public class SaajContentHandlerTest {

	private SaajContentHandler handler;

	private Transformer transformer;

	private SOAPEnvelope envelope;

	@BeforeEach
	public void setUp() throws Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage message = messageFactory.createMessage();
		envelope = message.getSOAPPart().getEnvelope();
		handler = new SaajContentHandler(envelope.getBody());
		transformer = TransformerFactoryUtils.newInstance().newTransformer();
	}

	@Test
	public void testHandler() throws Exception {

		String content = "<Root xmlns='http://springframework.org/spring-ws/1' "
				+ "xmlns:child='http://springframework.org/spring-ws/2'>"
				+ "<child:Child attribute='value'>Content</child:Child></Root>";
		Source source = new StringSource(content);
		Result result = new SAXResult(handler);
		transformer.transform(source, result);
		Name rootName = envelope.createName("Root", "", "http://springframework.org/spring-ws/1");
		Iterator<?> iterator = envelope.getBody().getChildElements(rootName);

		assertThat(iterator.hasNext()).isTrue();

		SOAPBodyElement rootElement = (SOAPBodyElement) iterator.next();
		Name childName = envelope.createName("Child", "child", "http://springframework.org/spring-ws/2");
		iterator = rootElement.getChildElements(childName);

		assertThat(iterator.hasNext()).isTrue();

		SOAPElement childElement = (SOAPElement) iterator.next();

		assertThat(childElement.getValue()).isEqualTo("Content");

		Name attributeName = envelope.createName("attribute");

		assertThat(childElement.getAttributeValue(attributeName)).isEqualTo("value");
	}
}
