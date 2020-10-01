/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.client;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerHelper;
import org.xmlunit.assertj.XmlAssert;

public class ResponseCreatorsTest {

	private final TransformerHelper transformerHelper = new TransformerHelper();

	private SaajSoapMessageFactory messageFactory;

	@BeforeEach
	public void createMessageFactory() {

		messageFactory = new SaajSoapMessageFactory();
		messageFactory.afterPropertiesSet();
	}

	@Test
	public void withPayloadSource() throws Exception {

		String payload = "<payload xmlns='http://springframework.org'/>";
		ResponseCreator responseCreator = ResponseCreators.withPayload(new StringSource(payload));

		WebServiceMessage response = responseCreator.createResponse(null, null, messageFactory);

		XmlAssert.assertThat(getPayloadAsString(response)).and(payload).ignoreWhitespace().areSimilar();
	}

	@Test
	public void withPayloadResource() throws Exception {

		String payload = "<payload xmlns='http://springframework.org'/>";
		ResponseCreator responseCreator = ResponseCreators.withPayload(new ByteArrayResource(payload.getBytes("UTF-8")));

		WebServiceMessage response = responseCreator.createResponse(null, null, messageFactory);

		XmlAssert.assertThat(getPayloadAsString(response)).and(payload).ignoreWhitespace().areSimilar();
	}

	@Test
	public void withSoapEnvelopeSource() throws Exception {

		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<?xml version='1.0'?>");
		xmlBuilder.append("<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>");
		xmlBuilder.append("<soap:Header><header xmlns='http://springframework.org'/></soap:Header>");
		xmlBuilder.append("<soap:Body><payload xmlns='http://springframework.org'/></soap:Body>");
		xmlBuilder.append("</soap:Envelope>");
		String envelope = xmlBuilder.toString();
		ResponseCreator responseCreator = ResponseCreators.withSoapEnvelope(new StringSource(envelope));
		WebServiceMessage response = responseCreator.createResponse(null, null, messageFactory);

		XmlAssert.assertThat(getSoapEnvelopeAsString((SoapMessage) response)).and(envelope).ignoreWhitespace().areSimilar();
	}

	@Test
	public void withSoapEnvelopeResource() throws Exception {

		StringBuilder xmlBuilder = new StringBuilder();
		xmlBuilder.append("<?xml version='1.0'?>");
		xmlBuilder.append("<soap:Envelope xmlns:soap='http://www.w3.org/2003/05/soap-envelope'>");
		xmlBuilder.append("<soap:Header><header xmlns='http://springframework.org'/></soap:Header>");
		xmlBuilder.append("<soap:Body><payload xmlns='http://springframework.org'/></soap:Body>");
		xmlBuilder.append("</soap:Envelope>");
		String envelope = xmlBuilder.toString();
		ResponseCreator responseCreator = ResponseCreators
				.withSoapEnvelope(new ByteArrayResource(envelope.getBytes("UTF-8")));
		WebServiceMessage response = responseCreator.createResponse(null, null, messageFactory);

		XmlAssert.assertThat(getSoapEnvelopeAsString((SoapMessage) response)).and(envelope).ignoreWhitespace().areSimilar();
	}

	@Test
	public void withIOException() {

		IOException expected = new IOException("Foo");
		ResponseCreator responseCreator = ResponseCreators.withException(expected);

		try {
			responseCreator.createResponse(null, null, null);
		} catch (IOException actual) {
			assertThat(actual).isSameAs(expected);
		}
	}

	@Test
	public void withRuntimeException() throws Exception {

		RuntimeException expected = new RuntimeException("Foo");
		ResponseCreator responseCreator = ResponseCreators.withException(expected);

		try {
			responseCreator.createResponse(null, null, null);
		} catch (RuntimeException actual) {
			assertThat(actual).isSameAs(expected);
		}
	}

	@Test
	public void withMustUnderstandFault() throws Exception {

		String faultString = "Foo";
		ResponseCreator responseCreator = ResponseCreators.withMustUnderstandFault(faultString, Locale.ENGLISH);

		testFault(responseCreator, faultString, SoapVersion.SOAP_11.getMustUnderstandFaultName());
	}

	@Test
	public void withClientOrSenderFault() throws Exception {

		String faultString = "Foo";
		ResponseCreator responseCreator = ResponseCreators.withClientOrSenderFault(faultString, Locale.ENGLISH);

		testFault(responseCreator, faultString, SoapVersion.SOAP_11.getClientOrSenderFaultName());
	}

	@Test
	public void withServerOrReceiverFault() throws Exception {

		String faultString = "Foo";
		ResponseCreator responseCreator = ResponseCreators.withServerOrReceiverFault(faultString, Locale.ENGLISH);

		testFault(responseCreator, faultString, SoapVersion.SOAP_11.getServerOrReceiverFaultName());
	}

	@Test
	public void withVersionMismatchFault() throws Exception {

		String faultString = "Foo";
		ResponseCreator responseCreator = ResponseCreators.withVersionMismatchFault(faultString, Locale.ENGLISH);

		testFault(responseCreator, faultString, SoapVersion.SOAP_11.getVersionMismatchFaultName());
	}

	private void testFault(ResponseCreator responseCreator, String faultString, QName faultCode) throws IOException {

		SoapMessage response = (SoapMessage) responseCreator.createResponse(null, null, messageFactory);

		assertThat(response.hasFault()).isTrue();

		Soap11Fault soapFault = (Soap11Fault) response.getSoapBody().getFault();

		assertThat(soapFault.getFaultCode()).isEqualTo(faultCode);
		assertThat(soapFault.getFaultStringOrReason()).isEqualTo(faultString);
		assertThat(soapFault.getFaultStringLocale()).isEqualTo(Locale.ENGLISH);
	}

	private String getPayloadAsString(WebServiceMessage message) throws TransformerException {

		Result result = new StringResult();
		transformerHelper.transform(message.getPayloadSource(), result);
		return result.toString();
	}

	private String getSoapEnvelopeAsString(SoapMessage message) throws TransformerException {

		DOMSource source = new DOMSource(message.getDocument());
		Result result = new StringResult();
		transformerHelper.transform(source, result);
		return result.toString();
	}
}
