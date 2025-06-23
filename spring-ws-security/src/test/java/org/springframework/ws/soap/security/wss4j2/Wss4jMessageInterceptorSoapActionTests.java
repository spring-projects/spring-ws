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

import java.util.Properties;

import org.apache.wss4j.dom.WSConstants;
import org.junit.jupiter.api.Test;

import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class Wss4jMessageInterceptorSoapActionTests extends Wss4jTests {

	private static final String SOAP_ACTION = "\"http://test\"";

	private Properties users;

	private Wss4jSecurityInterceptor interceptor;

	@Override
	protected void onSetup() throws Exception {

		this.users = new Properties();
		this.users.setProperty("Bert", "Ernie");
		this.interceptor = new Wss4jSecurityInterceptor();
		this.interceptor.setValidationActions("UsernameToken");
		this.interceptor.setSecurementActions("UsernameToken");
		this.interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
		SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
		callbackHandler.setUsers(this.users);
		this.interceptor.setValidationCallbackHandler(callbackHandler);

		this.interceptor.afterPropertiesSet();
	}

	@Test
	void testPreserveSoapActionOnValidation() throws Exception {

		SoapMessage message = loadSoap11Message("usernameTokenPlainText-soap.xml");
		message.setSoapAction(SOAP_ACTION);
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		this.interceptor.validateMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

	@Test
	void testPreserveSoap12ActionOnValidation() throws Exception {
		SoapMessage message = loadSoap12Message("usernameTokenPlainText-soap12.xml");
		message.setSoapAction(SOAP_ACTION);
		WebServiceMessageFactory messageFactory = getSoap12MessageFactory();
		MessageContext messageContext = new DefaultMessageContext(message, messageFactory);
		this.interceptor.validateMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

	@Test
	void testPreserveSoapActionOnSecurement() throws Exception {
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		message.setSoapAction(SOAP_ACTION);
		this.interceptor.setSecurementUsername("Bert");
		this.interceptor.setSecurementPassword("Ernie");
		MessageContext messageContext = getSoap11MessageContext(message);
		this.interceptor.secureMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

	@Test
	void testPreserveSoap12ActionOnSecurement() throws Exception {
		SoapMessage message = loadSoap12Message("empty-soap12.xml");
		message.setSoapAction(SOAP_ACTION);
		this.interceptor.setSecurementUsername("Bert");
		this.interceptor.setSecurementPassword("Ernie");
		MessageContext messageContext = getSoap12MessageContext(message);
		this.interceptor.secureMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

}
