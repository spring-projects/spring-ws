/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.xwss.callback;

import java.util.Collections;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ws.soap.security.callback.CleanupCallback;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class SpringPlainTextPasswordValidationCallbackHandlerTest {

	private SpringPlainTextPasswordValidationCallbackHandler callbackHandler;

	private AuthenticationManager authenticationManager;

	private PasswordValidationCallback callback;

	private String username;

	private String password;

	@Before
	public void setUp() throws Exception {
		callbackHandler = new SpringPlainTextPasswordValidationCallbackHandler();
		authenticationManager = createMock(AuthenticationManager.class);
		callbackHandler.setAuthenticationManager(authenticationManager);
		username = "Bert";
		password = "Ernie";
		PasswordValidationCallback.PlainTextPasswordRequest request =
				new PasswordValidationCallback.PlainTextPasswordRequest(username, password);
		callback = new PasswordValidationCallback(request);
	}

	@After
	public void tearDown() throws Exception {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testAuthenticateUserPlainTextValid() throws Exception {
		Authentication authResult = new TestingAuthenticationToken(username, password, Collections
						.<GrantedAuthority>emptyList());
		expect(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))).andReturn(authResult);

		replay(authenticationManager);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertTrue("Not authenticated", authenticated);
		Assert.assertNotNull("No Authentication created", SecurityContextHolder.getContext().getAuthentication());

		verify(authenticationManager);
	}

	@Test
	public void testAuthenticateUserPlainTextInvalid() throws Exception {
		expect(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))).andThrow(new BadCredentialsException(""));

		replay(authenticationManager);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertFalse("Authenticated", authenticated);
		Assert.assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());

		verify(authenticationManager);
	}

	@Test
	public void testCleanUp() throws Exception {
		TestingAuthenticationToken authentication =
				new TestingAuthenticationToken(new Object(), new Object(), Collections.<GrantedAuthority>emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		CleanupCallback cleanupCallback = new CleanupCallback();
		callbackHandler.handleInternal(cleanupCallback);
		Assert.assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
	}

}
