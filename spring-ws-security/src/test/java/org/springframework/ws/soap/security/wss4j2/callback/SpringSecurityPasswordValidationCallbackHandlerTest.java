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

package org.springframework.ws.soap.security.wss4j2.callback;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.principal.WSUsernameTokenPrincipalImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/** @author tareq */
public class SpringSecurityPasswordValidationCallbackHandlerTest {

	private SpringSecurityPasswordValidationCallbackHandler callbackHandler;

	private SimpleGrantedAuthority grantedAuthority;

	private UsernameTokenPrincipalCallback callback;
	
	private WSPasswordCallback passwordCallback;

	private UserDetails user;

	@Before
	public void setUp() throws Exception {
		callbackHandler = new SpringSecurityPasswordValidationCallbackHandler();

		grantedAuthority = new SimpleGrantedAuthority("ROLE_1");
		user = new User("Ernie", "Bert", true, true, true, true, Collections.singleton(grantedAuthority));

		WSUsernameTokenPrincipalImpl principal = new WSUsernameTokenPrincipalImpl("Ernie", true);
		callback = new UsernameTokenPrincipalCallback(principal);
		
		passwordCallback = new WSPasswordCallback("Ernie", null, "type", WSPasswordCallback.USERNAME_TOKEN);
	}
	
	@Test
	public void testHandleUsernameToken() throws Exception {
		UserDetailsService userDetailsService = createMock(UserDetailsService.class);
		callbackHandler.setUserDetailsService(userDetailsService);

		expect(userDetailsService.loadUserByUsername("Ernie")).andReturn(user).anyTimes();

		replay(userDetailsService);

		callbackHandler.handleUsernameToken(passwordCallback);
		Assert.assertEquals("Bert", passwordCallback.getPassword());

		verify(userDetailsService);
	}
	
	@Test
	public void testHandleUsernameTokenUserNotFound() throws Exception {
		UserDetailsService userDetailsService = createMock(UserDetailsService.class);
		callbackHandler.setUserDetailsService(userDetailsService);

		expect(userDetailsService.loadUserByUsername("Ernie")).andThrow(new UsernameNotFoundException("User 'Ernie' not found"));

		replay(userDetailsService);

		callbackHandler.handleUsernameToken(passwordCallback);
		Assert.assertNull(passwordCallback.getPassword());

		verify(userDetailsService);
	}

	@Test
	public void testHandleUsernameTokenPrincipal() throws Exception {
		UserDetailsService userDetailsService = createMock(UserDetailsService.class);
		callbackHandler.setUserDetailsService(userDetailsService);

		expect(userDetailsService.loadUserByUsername("Ernie")).andReturn(user).anyTimes();

		replay(userDetailsService);

		callbackHandler.handleUsernameTokenPrincipal(callback);
		SecurityContext context = SecurityContextHolder.getContext();
		Assert.assertNotNull("SecurityContext must not be null", context);
		Authentication authentication = context.getAuthentication();
		Assert.assertNotNull("Authentication must not be null", authentication);
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Assert.assertTrue("GrantedAuthority[] must not be null or empty",
				(authorities != null && authorities.size() > 0));
		Assert.assertEquals("Unexpected authority", grantedAuthority, authorities.iterator().next());

		verify(userDetailsService);
	}
}
