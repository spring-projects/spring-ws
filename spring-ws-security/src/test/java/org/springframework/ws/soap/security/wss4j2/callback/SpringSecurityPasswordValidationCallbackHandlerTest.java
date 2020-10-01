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

package org.springframework.ws.soap.security.wss4j2.callback;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.util.Collection;
import java.util.Collections;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.principal.WSUsernameTokenPrincipalImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** @author tareq */
public class SpringSecurityPasswordValidationCallbackHandlerTest {

	private SpringSecurityPasswordValidationCallbackHandler callbackHandler;

	private SimpleGrantedAuthority grantedAuthority;

	private UsernameTokenPrincipalCallback callback;

	private WSPasswordCallback passwordCallback;

	private UserDetails user;

	@BeforeEach
	public void setUp() {

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

		assertThat(passwordCallback.getPassword()).isEqualTo("Bert");

		verify(userDetailsService);
	}

	@Test
	public void testHandleUsernameTokenUserNotFound() throws Exception {

		UserDetailsService userDetailsService = createMock(UserDetailsService.class);
		callbackHandler.setUserDetailsService(userDetailsService);

		expect(userDetailsService.loadUserByUsername("Ernie"))
				.andThrow(new UsernameNotFoundException("User 'Ernie' not found"));

		replay(userDetailsService);

		callbackHandler.handleUsernameToken(passwordCallback);

		assertThat(passwordCallback.getPassword()).isNull();

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

		assertThat(context).isNotNull();

		Authentication authentication = context.getAuthentication();

		assertThat(authentication).isNotNull();

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		assertThat(authorities).isNotNull();
		assertThat(authorities).isNotEmpty();
		assertThat(authorities.iterator().next()).isEqualTo(grantedAuthority);

		verify(userDetailsService);
	}
}
