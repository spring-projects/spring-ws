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

package org.springframework.ws.soap.security.xwss.callback;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

public class SimplePasswordValidationCallbackHandlerTest {

	private SimplePasswordValidationCallbackHandler handler;

	@BeforeEach
	public void setUp() {

		handler = new SimplePasswordValidationCallbackHandler();
		Properties users = new Properties();
		users.setProperty("Bert", "Ernie");
		handler.setUsers(users);
	}

	@Test
	public void testPlainTextPasswordValid() throws Exception {

		PasswordValidationCallback.PlainTextPasswordRequest request = new PasswordValidationCallback.PlainTextPasswordRequest(
				"Bert", "Ernie");
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		handler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isTrue();
	}

	@Test
	public void testPlainTextPasswordInvalid() throws Exception {

		PasswordValidationCallback.PlainTextPasswordRequest request = new PasswordValidationCallback.PlainTextPasswordRequest(
				"Bert", "Big bird");
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		handler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isFalse();
	}

	@Test
	public void testPlainTextPasswordNoSuchUser() throws Exception {

		PasswordValidationCallback.PlainTextPasswordRequest request = new PasswordValidationCallback.PlainTextPasswordRequest(
				"Big bird", "Bert");
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		handler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isFalse();
	}

	@Test
	public void testDigestPasswordValid() throws Exception {

		String username = "Bert";
		String nonce = "9mdsYDCrjjYRur0rxzYt2oD7";
		String passwordDigest = "kwNstEaiFOrI7B31j7GuETYvdgk=";
		String creationTime = "2006-06-01T23:48:42Z";
		PasswordValidationCallback.DigestPasswordRequest request = new PasswordValidationCallback.DigestPasswordRequest(
				username, passwordDigest, nonce, creationTime);
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		handler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isTrue();
	}

	@Test
	public void testDigestPasswordInvalid() throws Exception {

		String username = "Bert";
		String nonce = "9mdsYDCrjjYRur0rxzYt2oD7";
		String passwordDigest = "kwNstEaiFOrI7B31j7GuETYvdgk";
		String creationTime = "2006-06-01T23:48:42Z";
		PasswordValidationCallback.DigestPasswordRequest request = new PasswordValidationCallback.DigestPasswordRequest(
				username, passwordDigest, nonce, creationTime);
		PasswordValidationCallback callback = new PasswordValidationCallback(request);
		handler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isFalse();
	}
}
