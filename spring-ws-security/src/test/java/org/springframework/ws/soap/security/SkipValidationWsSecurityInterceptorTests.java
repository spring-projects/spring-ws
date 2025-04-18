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

package org.springframework.ws.soap.security;

import java.io.IOException;
import java.io.InputStream;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SkipValidationWsSecurityInterceptorTests {

	private MessageFactory messageFactory;

	private AbstractWsSecurityInterceptor interceptor;

	private SaajSoapMessageFactory soapMessageFactory;

	@BeforeEach
	public void setUp() throws Exception {

		this.messageFactory = MessageFactory.newInstance();
		this.soapMessageFactory = new SaajSoapMessageFactory(this.messageFactory);
		this.interceptor = new AbstractWsSecurityInterceptor() {

			@Override
			protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecurityValidationException {
				fail("validation must be skipped.");
			}

			@Override
			protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
					throws WsSecuritySecurementException {
			}

			@Override
			protected void cleanUp() {
			}
		};
		this.interceptor.setSkipValidationIfNoHeaderPresent(true);
	}

	@Test
	public void testSkipValidationOnNoHeader() throws Exception {
		doTestSkipValidation("noHeader-soap.xml");
	}

	@Test
	public void testSkipValidationOnEmptyHeader() throws Exception {
		doTestSkipValidation("emptyHeader-soap.xml");
	}

	@Test
	public void testSkipValidationOnNoSecurityHeader() throws Exception {
		doTestSkipValidation("noSecurityHeader-soap.xml");
	}

	private void doTestSkipValidation(String fileName) throws Exception {

		SoapMessage message = loadSaajMessage(fileName);
		MessageContext messageContext = new DefaultMessageContext(message, this.soapMessageFactory);

		assertThat(this.interceptor.handleRequest(messageContext, null)).isTrue();
	}

	private SaajSoapMessage loadSaajMessage(String fileName) throws SOAPException, IOException {

		MimeHeaders mimeHeaders = new MimeHeaders();
		mimeHeaders.addHeader("Content-Type", "text/xml");
		Resource resource = new ClassPathResource(fileName, getClass());

		assertThat(resource.exists()).isTrue();

		try (InputStream is = resource.getInputStream()) {
			return new SaajSoapMessage(this.messageFactory.createMessage(mimeHeaders, is));
		}
	}

}
