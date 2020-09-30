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

package org.springframework.ws.soap.security.xwss.callback;

import static org.easymock.EasyMock.*;

import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ws.soap.security.callback.CleanupCallback;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

public class SpringDigestPasswordValidationCallbackHandlerTest {

	private SpringDigestPasswordValidationCallbackHandler callbackHandler;

	private UserDetailsService userDetailsService;

	private String username;

	private String password;

	private PasswordValidationCallback callback;

	@Before
	public void setUp() throws Exception {
		callbackHandler = new SpringDigestPasswordValidationCallbackHandler();
		userDetailsService = createMock(UserDetailsService.class);
		callbackHandler.setUserDetailsService(userDetailsService);
		username = "Bert";
		password = "Ernie";
		String nonce = "9mdsYDCrjjYRur0rxzYt2oD7";
		String passwordDigest = "kwNstEaiFOrI7B31j7GuETYvdgk=";
		String creationTime = "2006-06-01T23:48:42Z";
		PasswordValidationCallback.DigestPasswordRequest request = new PasswordValidationCallback.DigestPasswordRequest(
				username, passwordDigest, nonce, creationTime);
		callback = new PasswordValidationCallback(request);
	}

	@After
	public void tearDown() throws Exception {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testAuthenticateUserDigestUserNotFound() throws Exception {
		expect(userDetailsService.loadUserByUsername(username)).andThrow(new UsernameNotFoundException(username));

		replay(userDetailsService);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertFalse("Authenticated", authenticated);
		Assert.assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());

		verify(userDetailsService);
	}

	@Test
	public void testAuthenticateUserDigestValid() throws Exception {
		User user = new User(username, password, true, true, true, true, Collections.<GrantedAuthority> emptyList());
		expect(userDetailsService.loadUserByUsername(username)).andReturn(user);

		replay(userDetailsService);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertTrue("Not authenticated", authenticated);
		Assert.assertNotNull("No Authentication created", SecurityContextHolder.getContext().getAuthentication());

		verify(userDetailsService);
	}

	@Test
	public void testAuthenticateUserDigestValidInvalid() throws Exception {
		User user = new User(username, "Big bird", true, true, true, true, Collections.<GrantedAuthority> emptyList());
		expect(userDetailsService.loadUserByUsername(username)).andReturn(user);

		replay(userDetailsService);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertFalse("Authenticated", authenticated);
		Assert.assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());

		verify(userDetailsService);
	}

	@Test
	public void testAuthenticateUserDigestDisabled() throws Exception {
		User user = new User(username, "Ernie", false, true, true, true, Collections.<GrantedAuthority> emptyList());
		expect(userDetailsService.loadUserByUsername(username)).andReturn(user);

		replay(userDetailsService);

		try {
			callbackHandler.handleInternal(callback);
			Assert.fail("disabled user authenticated");
		} catch (DisabledException expected) {
			// expected
		}
		verify(userDetailsService);
	}

	@Test
	public void testCleanUp() throws Exception {
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(new Object(), new Object(),
				Collections.<GrantedAuthority> emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		CleanupCallback cleanupCallback = new CleanupCallback();
		callbackHandler.handleInternal(cleanupCallback);
		Assert.assertNull("Authentication created", SecurityContextHolder.getContext().getAuthentication());
	}

}
