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

package org.springframework.ws.soap.axiom;

import java.io.StringWriter;

import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.util.xml.StaxUtils;

import static org.xmlunit.assertj.XmlAssert.assertThat;

@SuppressWarnings("Since15")
class NonCachingPayloadTests {

	private Payload payload;

	private SOAPBody body;

	@BeforeEach
	final void setUp() {

		SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
		this.body = soapFactory.createSOAPBody();
		this.payload = new NonCachingPayload(this.body, soapFactory);
	}

	@Test
	void testDelegatingStreamWriter() throws Exception {

		XMLStreamWriter streamWriter = StaxUtils.getXMLStreamWriter(this.payload.getResult());

		String namespace = "http://springframework.org/spring-ws";
		streamWriter.setDefaultNamespace(namespace);
		streamWriter.writeStartElement(namespace, "root");
		streamWriter.writeDefaultNamespace(namespace);
		streamWriter.writeStartElement(namespace, "child");
		streamWriter.writeCharacters("text");
		streamWriter.writeEndElement();
		streamWriter.writeEndElement();
		streamWriter.flush();

		StringWriter writer = new StringWriter();
		this.body.serialize(writer, true);

		String expected = "<soapenv:Body xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>"
				+ "<root xmlns='http://springframework.org/spring-ws'>" + "<child>text</child>"
				+ "</root></soapenv:Body>";

		assertThat(writer.toString()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testDelegatingStreamWriterWriteEndDocument() throws Exception {

		XMLStreamWriter streamWriter = StaxUtils.getXMLStreamWriter(this.payload.getResult());

		String namespace = "http://springframework.org/spring-ws";
		streamWriter.setDefaultNamespace(namespace);
		streamWriter.writeStartElement(namespace, "root");
		streamWriter.writeDefaultNamespace(namespace);
		streamWriter.writeStartElement(namespace, "child");
		streamWriter.writeCharacters("text");
		streamWriter.writeEndDocument();
		streamWriter.flush();

		StringWriter writer = new StringWriter();
		this.body.serialize(writer, true);

		String expected = "<soapenv:Body xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>"
				+ "<root xmlns='http://springframework.org/spring-ws'>" + "<child>text</child>"
				+ "</root></soapenv:Body>";

		assertThat(writer.toString()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testDelegatingStreamWriterWriteEmptyElement() throws Exception {
		XMLStreamWriter streamWriter = StaxUtils.getXMLStreamWriter(this.payload.getResult());

		String namespace = "http://springframework.org/spring-ws";
		streamWriter.setDefaultNamespace(namespace);
		streamWriter.writeStartElement(namespace, "root");
		streamWriter.writeDefaultNamespace(namespace);
		streamWriter.writeEmptyElement(namespace, "child");
		streamWriter.writeEndElement();
		streamWriter.flush();

		StringWriter writer = new StringWriter();
		this.body.serialize(writer, true);

		String expected = "<soapenv:Body xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>"
				+ "<root xmlns='http://springframework.org/spring-ws'>" + "<child />" + "</root></soapenv:Body>";

		assertThat(writer.toString()).and(expected).ignoreWhitespace().areIdentical();
	}

}
