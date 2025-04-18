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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.springframework.xml.transform.TransformerFactoryUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpServletConnectionTests {

	private HttpServletConnection connection;

	private MockHttpServletRequest httpServletRequest;

	private MockHttpServletResponse httpServletResponse;

	private static final String HEADER_NAME = "RequestHeader";

	private static final String HEADER_VALUE = "RequestHeaderValue";

	private static final String CONTENT = "<Request xmlns='http://springframework.org/spring-ws/' />";

	private static final String SOAP_CONTENT = "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'><SOAP-ENV:Header/><SOAP-ENV:Body>"
			+ CONTENT + "</SOAP-ENV:Body></SOAP-ENV:Envelope>";

	private SaajSoapMessageFactory messageFactory;

	private TransformerFactory transformerFactory;

	@BeforeEach
	public void setUp() throws Exception {

		this.httpServletRequest = new MockHttpServletRequest();
		this.httpServletResponse = new MockHttpServletResponse();
		this.connection = new HttpServletConnection(this.httpServletRequest, this.httpServletResponse);
		MessageFactory saajMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		this.messageFactory = new SaajSoapMessageFactory(saajMessageFactory);
		this.transformerFactory = TransformerFactoryUtils.newInstance();
	}

	@Test
	public void receive() throws Exception {

		byte[] bytes = SOAP_CONTENT.getBytes(StandardCharsets.UTF_8);
		this.httpServletRequest.addHeader("Content-Type", "text/xml");
		this.httpServletRequest.addHeader("Content-Length", Integer.toString(bytes.length));
		this.httpServletRequest.addHeader(HEADER_NAME, HEADER_VALUE);
		this.httpServletRequest.setContent(bytes);
		SaajSoapMessage message = (SaajSoapMessage) this.connection.receive(this.messageFactory);

		assertThat(message).isNotNull();

		StringResult result = new StringResult();
		Transformer transformer = this.transformerFactory.newTransformer();
		transformer.transform(message.getPayloadSource(), result);

		XmlAssert.assertThat(result.toString()).and(CONTENT).ignoreWhitespace().areIdentical();

		SOAPMessage saajMessage = message.getSaajMessage();
		String[] headerValues = saajMessage.getMimeHeaders().getHeader(HEADER_NAME);

		assertThat(headerValues).isNotNull();
		assertThat(headerValues).hasSize(1);
		assertThat(headerValues[0]).isEqualTo(HEADER_VALUE);
	}

	@Test
	public void send() throws Exception {

		SaajSoapMessage message = this.messageFactory.createWebServiceMessage();
		SOAPMessage saajMessage = message.getSaajMessage();
		MimeHeaders mimeHeaders = saajMessage.getMimeHeaders();
		mimeHeaders.addHeader(HEADER_NAME, HEADER_VALUE);
		Transformer transformer = this.transformerFactory.newTransformer();
		transformer.transform(new StringSource(CONTENT), message.getPayloadResult());

		this.connection.send(message);

		assertThat(this.httpServletResponse.getHeader(HEADER_NAME)).isEqualTo(HEADER_VALUE);
		XmlAssert.assertThat(this.httpServletResponse.getContentAsString())
			.and(SOAP_CONTENT)
			.ignoreWhitespace()
			.areIdentical();
	}

	@Test
	public void faultCodes() throws IOException {

		this.connection.setFaultCode(SoapVersion.SOAP_11.getClientOrSenderFaultName());
		assertThat(this.httpServletResponse.getStatus()).isEqualTo(500);

		this.connection.setFaultCode(SoapVersion.SOAP_11.getServerOrReceiverFaultName());
		assertThat(this.httpServletResponse.getStatus()).isEqualTo(500);

		this.connection.setFaultCode(SoapVersion.SOAP_12.getClientOrSenderFaultName());
		assertThat(this.httpServletResponse.getStatus()).isEqualTo(400);

		this.connection.setFaultCode(SoapVersion.SOAP_12.getServerOrReceiverFaultName());
		assertThat(this.httpServletResponse.getStatus()).isEqualTo(500);
	}

}
