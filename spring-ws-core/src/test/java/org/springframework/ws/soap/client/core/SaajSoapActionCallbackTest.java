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

package org.springframework.ws.soap.client.core;

import java.io.IOException;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.support.SoapUtils;
import org.springframework.ws.transport.TransportConstants;

import static org.assertj.core.api.Assertions.assertThat;

public class SaajSoapActionCallbackTest {

	private SaajSoapMessageFactory saaj11Factory = new SaajSoapMessageFactory();

	private SaajSoapMessageFactory saaj12Factory = new SaajSoapMessageFactory();

	@BeforeEach
	public void init() throws SOAPException {

		MessageFactory messageFactory11 = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

		saaj11Factory.setSoapVersion(SoapVersion.SOAP_11);
		saaj11Factory.setMessageFactory(messageFactory11);
		saaj11Factory.afterPropertiesSet();

		MessageFactory messageFactory12 = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		saaj12Factory.setSoapVersion(SoapVersion.SOAP_12);
		saaj12Factory.setMessageFactory(messageFactory12);
		saaj12Factory.afterPropertiesSet();
	}

	@Test
	public void noSoapAction11ShouldProduceEmptySoapActionHeader() {

		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		String[] soapActionHeaders = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).containsExactly("\"\"");
	}

	@Test
	public void soapAction11ShouldProduceSoapActionHeader() throws IOException {

		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback("testAction");
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).containsExactly("\"testAction\"");
	}

	@Test
	public void emptySoapAction11() throws IOException {

		SaajSoapMessage message = saaj11Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback(null);
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).containsExactly("\"\"");
	}

	@Test
	public void noSoapAction12() {

		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		String[] soapActionHeaders = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_SOAP_ACTION);

		assertThat(soapActionHeaders).isNull();
	}

	@Test
	public void soapAction12ShouldProduceNoSoapActionHeader() throws IOException {

		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback("testAction");
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_SOAP_ACTION);
		String[] contentTypes = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_CONTENT_TYPE);

		assertThat(soapActionHeaders).isNull();
		assertThat(SoapUtils.extractActionFromContentType(contentTypes[0])).isEqualTo("\"testAction\"");
	}

	@Test
	public void emptySoapAction12ShouldProduceNoSoapActionHeader() throws IOException {

		SaajSoapMessage message = saaj12Factory.createWebServiceMessage();
		SoapActionCallback callback = new SoapActionCallback(null);
		callback.doWithMessage(message);
		String[] soapActionHeaders = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_SOAP_ACTION);
		String[] contentTypes = message.getSaajMessage()
			.getMimeHeaders()
			.getHeader(TransportConstants.HEADER_CONTENT_TYPE);

		assertThat(soapActionHeaders).isNull();
		assertThat(SoapUtils.extractActionFromContentType(contentTypes[0])).isEqualTo("\"\"");
	}

}
