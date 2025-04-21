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

package org.springframework.ws.soap;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.AbstractMimeMessageTests;
import org.springframework.ws.mime.MimeMessage;
import org.springframework.ws.stream.StreamingPayload;
import org.springframework.ws.stream.StreamingWebServiceMessage;
import org.springframework.ws.transport.MockTransportOutputStream;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class AbstractSoapMessageTests extends AbstractMimeMessageTests {

	protected abstract String getNS();

	protected String getHeader() {
		return "<" + getNS() + ":Header/>";
	}

	protected SoapMessage soapMessage;

	@Override
	protected MimeMessage createMimeMessage() throws Exception {

		this.soapMessage = createSoapMessage();
		return this.soapMessage;
	}

	protected abstract SoapMessage createSoapMessage() throws Exception;

	@Test
	void testValidate() throws Exception {

		XmlValidator validator = XmlValidatorFactory.createValidator(getSoapSchemas(),
				XmlValidatorFactory.SCHEMA_W3C_XML);
		SAXParseException[] errors = validator.validate(this.soapMessage.getEnvelope().getSource());

		if (errors.length > 0) {
			fail(StringUtils.arrayToCommaDelimitedString(errors));
		}
	}

	@Test
	void testSoapAction() {

		assertThat(this.soapMessage.getSoapAction()).isEqualTo("\"\"");

		this.soapMessage.setSoapAction("SoapAction");

		assertThat(this.soapMessage.getSoapAction()).isEqualTo("\"SoapAction\"");
	}

	@Test
	void testCharsetAttribute() throws Exception {

		MockTransportOutputStream outputStream = new MockTransportOutputStream(new ByteArrayOutputStream());
		this.soapMessage.writeTo(outputStream);
		Map<String, String> headers = outputStream.getHeaders();
		String contentType = headers.get(TransportConstants.HEADER_CONTENT_TYPE);

		if (contentType != null) {

			Pattern charsetPattern = Pattern.compile("charset\\s*=\\s*([^;]+)");
			Matcher matcher = charsetPattern.matcher(contentType);

			if (matcher.find() && matcher.groupCount() == 1) {

				String charset = matcher.group(1).trim();
				assertThat(charset.indexOf('"')).isLessThan(0);
			}
		}
	}

	@Test
	void testSetStreamingPayload() throws Exception {

		if (!(this.soapMessage instanceof StreamingWebServiceMessage streamingMessage)) {
			return;
		}

		final QName name = new QName("http://springframework.org", "root", "");

		streamingMessage.setStreamingPayload(new StreamingPayload() {
			public QName getName() {
				return name;
			}

			public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
				// Use a prefix that is different from the one reported by getName()
				streamWriter.writeStartElement("prefix", name.getLocalPart(), name.getNamespaceURI());
				streamWriter.writeNamespace("prefix", name.getNamespaceURI());
				streamWriter.writeStartElement(name.getNamespaceURI(), "child");
				streamWriter.writeCharacters("Foo");
				streamWriter.writeEndElement();
				streamWriter.writeEndElement();
			}
		});

		StringResult result = new StringResult();
		this.transformer.transform(streamingMessage.getPayloadSource(), result);

		String expected = "<root xmlns='http://springframework.org'><child>Foo</child></root>";

		XmlAssert.assertThat(result.toString()).and(expected).ignoreWhitespace().areSimilar();

		this.soapMessage.writeTo(new ByteArrayOutputStream());
	}

	protected abstract Resource[] getSoapSchemas();

	@Test
	protected abstract void testGetVersion();

	@Test
	protected abstract void testWriteToTransportOutputStream() throws Exception;

	@Test
	protected abstract void testWriteToTransportResponseAttachment() throws Exception;

	@Test
	protected abstract void testToDocument() throws Exception;

	@Test
	protected abstract void testSetLiveDocument() throws Exception;

	@Test
	protected abstract void testSetOtherDocument() throws Exception;

}
