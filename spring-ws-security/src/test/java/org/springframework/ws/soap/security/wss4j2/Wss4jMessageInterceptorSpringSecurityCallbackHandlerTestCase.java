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

package org.springframework.ws.soap.security.wss4j2;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.util.Properties;

import org.apache.wss4j.dom.WSConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.callback.SpringSecurityPasswordValidationCallbackHandler;

public abstract class Wss4jMessageInterceptorSpringSecurityCallbackHandlerTestCase extends Wss4jTestCase {

	private Properties users = new Properties();

	private AuthenticationManager authenticationManager;

	@Override
	protected void onSetup() {

		authenticationManager = createMock(AuthenticationManager.class);
		users.setProperty("Bert", "Ernie,ROLE_TEST");
	}

	@AfterEach
	public void tearDown() {

		verify(authenticationManager);
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testValidateUsernameTokenPlainText() throws Exception {

		EndpointInterceptor interceptor = prepareInterceptor("UsernameToken", true, false);
		SoapMessage message = loadSoap11Message("usernameTokenPlainText-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		interceptor.handleRequest(messageContext, null);
		assertValidateUsernameToken(message);

		// test clean up
		messageContext.getResponse();
		interceptor.handleResponse(messageContext, null);
		interceptor.afterCompletion(messageContext, null, null);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
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
		interceptor.handleRequest(messageContext, null);
		assertValidateUsernameToken(message);

		// test clean up
		messageContext.getResponse();
		interceptor.handleResponse(messageContext, null);
		interceptor.afterCompletion(messageContext, null, null);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	protected void assertValidateUsernameToken(SoapMessage message) {

		Object result = getMessage(message);

		assertThat(result).isNotNull();
		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
	}

	protected Wss4jSecurityInterceptor prepareInterceptor(String actions, boolean validating, boolean digest)
			throws Exception {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();

		if (validating) {
			interceptor.setValidationActions(actions);
		} else {
			interceptor.setSecurementActions(actions);
		}

		SpringSecurityPasswordValidationCallbackHandler callbackHandler = new SpringSecurityPasswordValidationCallbackHandler();
		InMemoryUserDetailsManager userDetailsManager = new InMemoryUserDetailsManager(users);
		callbackHandler.setUserDetailsService(userDetailsManager);

		if (digest) {
			interceptor.setSecurementPasswordType(WSConstants.PW_DIGEST);
		} else {
			interceptor.setSecurementPasswordType(WSConstants.PW_TEXT);
		}

		interceptor.setValidationCallbackHandler(callbackHandler);
		interceptor.afterPropertiesSet();
		replay(authenticationManager);

		return interceptor;
	}
}
