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

package org.springframework.ws.soap.soap12;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.AbstractSoapMessageTest;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportOutputStream;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSoap12MessageTest extends AbstractSoapMessageTest {

	@Override
	public void testGetVersion() {
		assertThat(this.soapMessage.getVersion()).isEqualTo(SoapVersion.SOAP_12);
	}

	@Override
	protected final Resource[] getSoapSchemas() {

		return new Resource[] { new ClassPathResource("xml.xsd", AbstractSoap12MessageTest.class),
				new ClassPathResource("soap12.xsd", AbstractSoap12MessageTest.class) };
	}

	@Override
	public void testWriteToTransportOutputStream() throws Exception {

		SoapBody body = this.soapMessage.getSoapBody();
		String payload = "<payload xmlns='http://www.springframework.org' />";
		this.transformer.transform(new StringSource(payload), body.getPayloadResult());

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MockTransportOutputStream tos = new MockTransportOutputStream(bos);
		String soapAction = "http://springframework.org/spring-ws/Action";
		this.soapMessage.setSoapAction(soapAction);
		this.soapMessage.writeTo(tos);
		String result = bos.toString(StandardCharsets.UTF_8);

		XmlAssert.assertThat(result)
			.and("<" + getNS() + ":Envelope xmlns:" + getNS() + "='http://www.w3.org/2003/05/soap-envelope'>"
					+ getHeader() + "<" + getNS() + ":Body><payload xmlns='http://www.springframework.org' /></"
					+ getNS() + ":Body></" + getNS() + ":Envelope>")
			.ignoreWhitespace()
			.areIdentical();

		String contentType = tos.getHeaders().get(TransportConstants.HEADER_CONTENT_TYPE);

		assertThat(contentType).contains(SoapVersion.SOAP_12.getContentType());
		assertThat(tos.getHeaders()).doesNotContainKey(TransportConstants.HEADER_SOAP_ACTION);
		assertThat(contentType).contains(soapAction);

		String resultAccept = tos.getHeaders().get("Accept");

		assertThat(resultAccept).isNotNull();
	}

	@Override
	public void testWriteToTransportResponseAttachment() throws Exception {

		InputStreamSource inputStreamSource = new ByteArrayResource("contents".getBytes(StandardCharsets.UTF_8));
		this.soapMessage.addAttachment("contentId", inputStreamSource, "text/plain");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MockTransportOutputStream tos = new MockTransportOutputStream(bos);
		this.soapMessage.writeTo(tos);
		String contentType = tos.getHeaders().get("Content-Type");

		assertThat(contentType).contains("multipart/related");
		assertThat(contentType).contains("type=\"application/soap+xml\"");
	}

	@Override
	public void testToDocument() throws Exception {

		this.transformer.transform(new StringSource("<payload xmlns='http://www.springframework.org' />"),
				this.soapMessage.getSoapBody().getPayloadResult());

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expected = documentBuilder.newDocument();
		Element envelope = expected.createElementNS("http://www.w3.org/2003/05/soap-envelope", "Envelope");
		expected.appendChild(envelope);

		if (!getHeader().isEmpty()) {
			Element header = expected.createElementNS("http://www.w3.org/2003/05/soap-envelope", "Header");
			envelope.appendChild(header);
		}

		Element body = expected.createElementNS("http://www.w3.org/2003/05/soap-envelope", "Body");
		envelope.appendChild(body);
		Element payload = expected.createElementNS("http://www.springframework.org", "payload");
		body.appendChild(payload);

		Document result = this.soapMessage.getDocument();

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Override
	public void testSetLiveDocument() throws Exception {

		this.transformer.transform(new StringSource("<payload xmlns='http://www.springframework.org' />"),
				this.soapMessage.getSoapBody().getPayloadResult());

		Document document = this.soapMessage.getDocument();

		this.soapMessage.setDocument(document);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		this.soapMessage.writeTo(bos);

		String result = bos.toString(StandardCharsets.UTF_8);

		XmlAssert.assertThat(result)
			.and("<" + getNS() + ":Envelope xmlns:" + getNS() + "='http://www.w3.org/2003/05/soap-envelope'>"
					+ getHeader() + "<" + getNS() + ":Body><payload xmlns='http://www.springframework.org' /></"
					+ getNS() + ":Body></" + getNS() + ":Envelope>")
			.ignoreWhitespace()
			.areIdentical();
	}

	@Override
	public void testSetOtherDocument() throws Exception {

		this.transformer.transform(new StringSource("<payload xmlns='http://www.springframework.org' />"),
				this.soapMessage.getSoapBody().getPayloadResult());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		this.soapMessage.writeTo(bos);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		DOMResult domResult = new DOMResult();
		this.transformer.transform(new StreamSource(bis), domResult);

		Document document = (Document) domResult.getNode();

		this.soapMessage.setDocument(document);

		bos = new ByteArrayOutputStream();
		this.soapMessage.writeTo(bos);

		String result = bos.toString(StandardCharsets.UTF_8);

		XmlAssert.assertThat(result)
			.and("<" + getNS() + ":Envelope xmlns:" + getNS() + "='http://www.w3.org/2003/05/soap-envelope'>"
					+ getHeader() + "<" + getNS() + ":Body><payload xmlns='http://www.springframework.org' /></"
					+ getNS() + ":Body></" + getNS() + ":Envelope>")
			.ignoreWhitespace()
			.areIdentical();
	}

}
