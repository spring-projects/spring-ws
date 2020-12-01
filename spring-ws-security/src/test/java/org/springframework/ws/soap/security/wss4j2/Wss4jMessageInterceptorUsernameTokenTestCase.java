/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.apache.wss4j.dom.WSConstants;
import org.junit.jupiter.api.Test;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.w3c.dom.Document;

public abstract class Wss4jMessageInterceptorUsernameTokenTestCase extends Wss4jTestCase {

	private Properties users = new Properties();

	@Override
	protected void onSetup() throws Exception {
		users.setProperty("Bert", "Ernie");
	}

	@Test
	public void testValidateUsernameTokenPlainText() throws Exception {

		Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
		SoapMessage message = loadSoap11Message("usernameTokenPlainText-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.validateMessage(message, messageContext);
		assertValidateUsernameToken(message);
	}

	@Test
	public void testValidateUsernameTokenDigest() throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		interceptor.setSecurementActions("UsernameToken");
		interceptor.setSecurementUsername("Bert");
		interceptor.setSecurementPassword("Ernie");
		interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);

		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.handleRequest(messageContext);

		interceptor = prepareInterceptor("UsernameToken", true, true);
		interceptor.validateMessage(message, messageContext);

		assertValidateUsernameToken(message);
	}

	@Test
	public void testValidateUsernameTokenWithQualifiedType() throws Exception {

		Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
		SoapMessage message = loadSoap11Message("usernameTokenPlainTextQualifiedType-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.validateMessage(message, messageContext);

		assertValidateUsernameToken(message);
	}

	@Test
	public void testAddUsernameTokenPlainText() throws Exception {
		Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", false, false);
		interceptor.setSecurementUsername("Bert");
		interceptor.setSecurementPassword("Ernie");
		SoapMessage message = loadSoap11Message("empty-soap.xml");

		MessageContext messageContext = getSoap11MessageContext(message);

		interceptor.secureMessage(message, messageContext);

		assertAddUsernameTokenPlainText(message);
	}

	@Test
	public void testAddUsernameTokenDigest() throws Exception {

		Wss4jSecurityInterceptor interceptor = prepareInterceptor("UsernameToken", false, true);
		interceptor.setSecurementUsername("Bert");
		interceptor.setSecurementPassword("Ernie");
		SoapMessage message = loadSoap11Message("empty-soap.xml");

		MessageContext messageContext = getSoap11MessageContext(message);
		interceptor.secureMessage(message, messageContext);

		assertAddUsernameTokenDigest(message);
	}

	protected void assertValidateUsernameToken(SoapMessage message) {

		Object result = getMessage(message);

		assertThat(result).isNotNull();
		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
	}

	protected void assertAddUsernameTokenPlainText(SoapMessage message) {

		Object result = getMessage(message);

		assertThat(result).isNotNull();

		Document doc = getDocument(message);

		assertXpathEvaluatesTo("Invalid Username", "Bert",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", doc);
		assertXpathEvaluatesTo("Invalid Password", "Ernie",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
				doc);
	}

	protected void assertAddUsernameTokenDigest(SoapMessage message) {

		Object result = getMessage(message);
		Document doc = getDocument(message);

		assertThat(result).isNotNull();
		assertXpathEvaluatesTo("Invalid Username", "Bert",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", doc);
		assertXpathExists("Password does not exist",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']",
				doc);
	}

	protected Wss4jSecurityInterceptor prepareInterceptor(String actions, boolean validating, boolean digest)
			throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
		if (validating) {
			interceptor.setValidationActions(actions);
		} else {
			interceptor.setSecurementActions(actions);
		}
		SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();
		callbackHandler.setUsers(users);
		if (digest) {
			interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
		} else {
			interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
		}
		interceptor.setValidationCallbackHandler(callbackHandler);

		interceptor.setBspCompliant(false);

		interceptor.afterPropertiesSet();
		return interceptor;
	}
}
