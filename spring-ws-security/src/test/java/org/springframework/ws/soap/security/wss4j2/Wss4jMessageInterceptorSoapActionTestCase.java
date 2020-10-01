/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j2;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.apache.wss4j.dom.WSConstants;
import org.junit.jupiter.api.Test;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorSoapActionTestCase extends Wss4jTestCase {

	private static final String SOAP_ACTION = "\"http://test\"";

	private Properties users;

	private Wss4jSecurityInterceptor interceptor;

	@Override
	protected void onSetup() throws Exception {

		users = new Properties();
		users.setProperty("Bert", "Ernie");
		interceptor = new Wss4jSecurityInterceptor();
		interceptor.setValidationActions("UsernameToken");
		interceptor.setSecurementActions("UsernameToken");
		interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
		SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
		callbackHandler.setUsers(users);
		interceptor.setValidationCallbackHandler(callbackHandler);

		interceptor.afterPropertiesSet();
	}

	@Test
	public void testPreserveSoapActionOnValidation() throws Exception {

		SoapMessage message = loadSoap11Message("usernameTokenPlainText-soap.xml");
		message.setSoapAction(SOAP_ACTION);
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.validateMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

	@Test
	public void testPreserveSoap12ActionOnValidation() throws Exception {
		SoapMessage message = loadSoap12Message("usernameTokenPlainText-soap12.xml");
		message.setSoapAction(SOAP_ACTION);
		WebServiceMessageFactory messageFactory = getSoap12MessageFactory();
		MessageContext messageContext = new DefaultMessageContext(message, messageFactory);
		interceptor.validateMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

	@Test
	public void testPreserveSoapActionOnSecurement() throws Exception {
		SoapMessage message = loadSoap11Message("empty-soap.xml");
		message.setSoapAction(SOAP_ACTION);
		interceptor.setSecurementUsername("Bert");
		interceptor.setSecurementPassword("Ernie");
		MessageContext messageContext = getSoap11MessageContext(message);
		interceptor.secureMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}

	@Test
	public void testPreserveSoap12ActionOnSecurement() throws Exception {
		SoapMessage message = loadSoap12Message("empty-soap12.xml");
		message.setSoapAction(SOAP_ACTION);
		interceptor.setSecurementUsername("Bert");
		interceptor.setSecurementPassword("Ernie");
		MessageContext messageContext = getSoap12MessageContext(message);
		interceptor.secureMessage(message, messageContext);

		assertThat(message.getSoapAction()).isNotNull();
		assertThat(message.getSoapAction()).isEqualTo(SOAP_ACTION);
	}
}
