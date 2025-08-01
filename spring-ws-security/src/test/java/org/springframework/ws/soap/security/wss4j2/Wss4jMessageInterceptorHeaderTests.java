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

package org.springframework.ws.soap.security.wss4j2;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.WsSecurityValidationException;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Arjen Poutsma
 * @author Tareq Abedrabbo
 * @author Greg Turnquist
 */
public abstract class Wss4jMessageInterceptorHeaderTests extends Wss4jTests {

	private Wss4jSecurityInterceptor interceptor;

	private Wss4jSecurityInterceptor interceptorThatKeepsSecurityHeader;

	@Override
	protected void onSetup() throws Exception {

		Properties users = new Properties();
		users.setProperty("Bert", "Ernie");
		this.interceptor = new Wss4jSecurityInterceptor();
		this.interceptor.setValidateRequest(true);
		this.interceptor.setSecureResponse(true);
		this.interceptor.setValidationActions("UsernameToken");
		SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
		callbackHandler.setUsers(users);
		this.interceptor.setValidationCallbackHandler(callbackHandler);
		this.interceptor.afterPropertiesSet();

		this.interceptorThatKeepsSecurityHeader = new Wss4jSecurityInterceptor();
		this.interceptorThatKeepsSecurityHeader.setValidateRequest(true);
		this.interceptorThatKeepsSecurityHeader.setSecureResponse(true);
		this.interceptorThatKeepsSecurityHeader.setValidationActions("UsernameToken");
		this.interceptorThatKeepsSecurityHeader.setValidationCallbackHandler(callbackHandler);
		this.interceptorThatKeepsSecurityHeader.setRemoveSecurityHeader(false);
		this.interceptorThatKeepsSecurityHeader.afterPropertiesSet();
	}

	@Test
	void testValidateUsernameTokenPlainText() throws Exception {

		SoapMessage message = loadSoap11Message("usernameTokenPlainTextWithHeaders-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		this.interceptor.validateMessage(message, messageContext);
		Object result = getMessage(message);

		assertThat(result).isNotNull();

		for (Iterator<SoapHeaderElement> i = message.getEnvelope().getHeader().examineAllHeaderElements(); i
			.hasNext();) {

			SoapHeaderElement element = i.next();
			QName name = element.getName();
			if (name.getNamespaceURI()
				.equals("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")) {
				fail("Security Header not removed");
			}
		}

		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
		assertXpathExists("header1 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header1", getDocument(message));
		assertXpathExists("header2 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header2", getDocument(message));
	}

	@Test
	void testValidateUsernameTokenPlainTextButKeepSecurityHeader() throws Exception {

		SoapMessage message = loadSoap11Message("usernameTokenPlainTextWithHeaders-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		this.interceptorThatKeepsSecurityHeader.validateMessage(message, messageContext);
		Object result = getMessage(message);

		assertThat(result).isNotNull();

		boolean foundSecurityHeader = false;
		for (Iterator<SoapHeaderElement> i = message.getEnvelope().getHeader().examineAllHeaderElements(); i
			.hasNext();) {

			SoapHeaderElement element = i.next();
			QName name = element.getName();
			if (name.getNamespaceURI()
				.equals("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")) {
				foundSecurityHeader = true;
			}

		}

		assertThat(foundSecurityHeader).isTrue();
		assertXpathExists("header1 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header1", getDocument(message));
		assertXpathExists("header2 not found", "/SOAP-ENV:Envelope/SOAP-ENV:Header/header2", getDocument(message));
	}

	@Test
	void testEmptySecurityHeader() {

		assertThatExceptionOfType(WsSecurityValidationException.class).isThrownBy(() -> {

			SoapMessage message = loadSoap11Message("emptySecurityHeader-soap.xml");
			MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
			this.interceptor.validateMessage(message, messageContext);
		});
	}

	@Test
	void testPreserveCustomHeaders() throws Exception {

		this.interceptor.setSecurementActions("UsernameToken");
		this.interceptor.setSecurementUsername("Bert");
		this.interceptor.setSecurementPassword("Ernie");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		SoapMessage message = loadSoap11Message("customHeader-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		message.writeTo(os);
		String document = os.toString(StandardCharsets.UTF_8);

		assertXpathEvaluatesTo("Header 1 does not exist", "test1", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header1",
				document);
		assertXpathNotExists("Header 2 exist", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header2", document);

		this.interceptor.secureMessage(message, messageContext);

		SoapHeaderElement element = message.getSoapHeader().addHeaderElement(new QName("http://test", "header2"));
		element.setText("test2");

		os = new ByteArrayOutputStream();
		message.writeTo(os);
		document = os.toString(StandardCharsets.UTF_8);

		assertXpathEvaluatesTo("Header 1 does not exist", "test1", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header1",
				document);
		assertXpathEvaluatesTo("Header 2 does not exist", "test2", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header2",
				document);

		os = new ByteArrayOutputStream();
		message.writeTo(os);
		document = os.toString(StandardCharsets.UTF_8);

		assertXpathEvaluatesTo("Header 1 does not exist", "test1", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header1",
				document);
		assertXpathEvaluatesTo("Header 2 does not exist", "test2", "/SOAP-ENV:Envelope/SOAP-ENV:Header/test:header2",
				document);
	}

}
