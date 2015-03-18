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

package org.springframework.ws.soap.security.xwss.callback.jaas;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JaasPlainTextPasswordValidationCallbackHandlerTest {

	private JaasPlainTextPasswordValidationCallbackHandler callbackHandler;

	@Before
	public void setUp() throws Exception {
		System.setProperty("java.security.auth.login.config", getClass().getResource("jaas.config").toString());
		callbackHandler = new JaasPlainTextPasswordValidationCallbackHandler();
		callbackHandler.setLoginContextName("PlainText");
	}

	@Test
	public void testAuthenticateUserPlainTextValid() throws Exception {
		PasswordValidationCallback.PlainTextPasswordRequest request =
				new PasswordValidationCallback.PlainTextPasswordRequest("Bert", "Ernie");
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertTrue("Not authenticated", authenticated);
	}

	@Test
	public void testAuthenticateUserPlainTextInvalid() throws Exception {
		PasswordValidationCallback.PlainTextPasswordRequest request =
				new PasswordValidationCallback.PlainTextPasswordRequest("Bert", "Big bird");
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertFalse("Authenticated", authenticated);
	}

}