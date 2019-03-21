/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.soap11;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.AbstractSoapMessageTestCase;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.MockTransportOutputStream;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.DocumentBuilderFactoryUtils;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractSoap11MessageTestCase extends AbstractSoapMessageTestCase {

	@Override
	protected final Resource[] getSoapSchemas() {
		return new Resource[]{new ClassPathResource("soap11.xsd", AbstractSoap11MessageTestCase.class)};
	}

	@Override
	public void testGetVersion() throws Exception {
		Assert.assertEquals("Invalid SOAP version", SoapVersion.SOAP_11, soapMessage.getVersion());
	}

	@Override
	public void testWriteToTransportOutputStream() throws Exception {
		SoapBody body = soapMessage.getSoapBody();
		String payload = "<payload xmlns='https://www.springframework.org' />";
		transformer.transform(new StringSource(payload), body.getPayloadResult());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MockTransportOutputStream tos = new MockTransportOutputStream(bos);
		String soapAction = "http://springframework.org/spring-ws/Action";
		soapMessage.setSoapAction(soapAction);
		soapMessage.writeTo(tos);
		String result = bos.toString("UTF-8");
		assertXMLEqual(
				"<" + getNS() + ":Envelope xmlns:" + getNS() + "='http://schemas.xmlsoap.org/soap/envelope/'>" + getHeader() + "<" + getNS() + ":Body><payload xmlns='https://www.springframework.org' /></" + getNS() + ":Body></" + getNS() + ":Envelope>",
				result);
		String contentType = tos.getHeaders().get("Content-Type");
		assertTrue("Invalid Content-Type set", contentType.indexOf(SoapVersion.SOAP_11.getContentType()) != -1);
		String resultSoapAction = tos.getHeaders().get("SOAPAction");
		assertEquals("Invalid soap action", "\"" + soapAction + "\"", resultSoapAction);
		String resultAccept = tos.getHeaders().get("Accept");
		assertNotNull("Invalid accept header", resultAccept);
	}

	@Override
	public void testWriteToTransportResponseAttachment() throws Exception {
		InputStreamSource inputStreamSource = new ByteArrayResource("contents".getBytes("UTF-8"));
		soapMessage.addAttachment("contentId", inputStreamSource, "text/plain");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MockTransportOutputStream tos = new MockTransportOutputStream(bos);
		soapMessage.writeTo(tos);
		String contentType = tos.getHeaders().get("Content-Type");
		assertTrue("Content-Type for attachment message does not contains multipart/related",
				contentType.indexOf("multipart/related") != -1);
		assertTrue("Content-Type for attachment message does not contains type=\"text/xml\"",
				contentType.indexOf("type=\"text/xml\"") != -1);
	}

	@Override
	public void testToDocument() throws Exception {
		transformer.transform(new StringSource("<payload xmlns='https://www.springframework.org' />"),
				soapMessage.getSoapBody().getPayloadResult());

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document expected = documentBuilder.newDocument();
		Element envelope = expected.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
		expected.appendChild(envelope);

		if (!getHeader().isEmpty()) {
			Element header = expected.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
			envelope.appendChild(header);
		}

		Element body = expected.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
		envelope.appendChild(body);
		Element payload = expected.createElementNS("https://www.springframework.org", "payload");
		body.appendChild(payload);

		Document result = soapMessage.getDocument();

		assertXMLEqual(expected, result);
	}

	@Override
	public void testSetLiveDocument() throws Exception {
		transformer.transform(new StringSource("<payload xmlns='https://www.springframework.org' />"),
				soapMessage.getSoapBody().getPayloadResult());

		Document document = soapMessage.getDocument();

		soapMessage.setDocument(document);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		soapMessage.writeTo(bos);

		String result = bos.toString("UTF-8");
		assertXMLEqual(
				"<" + getNS() + ":Envelope xmlns:" + getNS() + "='http://schemas.xmlsoap.org/soap/envelope/'>" + getHeader() + "<" + getNS() + ":Body><payload xmlns='https://www.springframework.org' /></" + getNS() + ":Body></" + getNS() + ":Envelope>",
				result);
	}

	@Override
	public void testSetOtherDocument() throws Exception {
		transformer.transform(new StringSource("<payload xmlns='https://www.springframework.org' />"),
				soapMessage.getSoapBody().getPayloadResult());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		soapMessage.writeTo(bos);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		DOMResult domResult = new DOMResult();
		transformer.transform(new StreamSource(bis), domResult);

		Document document = (Document) domResult.getNode();

		soapMessage.setDocument(document);

		bos = new ByteArrayOutputStream();
		soapMessage.writeTo(bos);

		String result = bos.toString("UTF-8");
		assertXMLEqual(
				"<" + getNS() + ":Envelope xmlns:" + getNS() + "='http://schemas.xmlsoap.org/soap/envelope/'>" + getHeader() + "<" + getNS() + ":Body><payload xmlns='https://www.springframework.org' /></" + getNS() + ":Body></" + getNS() + ":Envelope>",
				result);
	}

}
