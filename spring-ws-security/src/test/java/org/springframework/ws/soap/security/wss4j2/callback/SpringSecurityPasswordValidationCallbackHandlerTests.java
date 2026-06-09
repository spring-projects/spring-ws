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

package org.springframework.ws.soap.security.wss4j2.callback;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author tareq
 */
class SpringSecurityPasswordValidationCallbackHandlerTests {

	private SpringSecurityPasswordValidationCallbackHandler callbackHandler;

	private SimpleGrantedAuthority grantedAuthority;

	private UsernameTokenPrincipalCallback callback;

	private WSPasswordCallback passwordCallback;

	private UserDetails user;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();

		this.callbackHandler = new SpringSecurityPasswordValidationCallbackHandler();

		this.grantedAuthority = new SimpleGrantedAuthority("ROLE_1");
		this.user = new User("Ernie", "Bert", true, true, true, true, Collections.singleton(this.grantedAuthority));

		WSUsernameTokenPrincipalImpl principal = new WSUsernameTokenPrincipalImpl("Ernie", true);
		this.callback = new UsernameTokenPrincipalCallback(principal);

		this.passwordCallback = new WSPasswordCallback("Ernie", null, "type", WSPasswordCallback.USERNAME_TOKEN);
	}

	@Test
	void handleUsernameTokenLoadsPasswordWhenUserValid() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);

		when(userDetailsService.loadUserByUsername("Ernie")).thenReturn(this.user);

		this.callbackHandler.handleUsernameToken(this.passwordCallback);

		assertThat(this.passwordCallback.getPassword()).isEqualTo("Bert");
		verify(userDetailsService).loadUserByUsername("Ernie");
	}

	@Test
	void handleUsernameTokenLeavesPasswordUnsetWhenUserNotFound() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);

		when(userDetailsService.loadUserByUsername("Ernie"))
			.thenThrow(new UsernameNotFoundException("User 'Ernie' not found"));

		this.callbackHandler.handleUsernameToken(this.passwordCallback);

		assertThat(this.passwordCallback.getPassword()).isNull();
		verify(userDetailsService).loadUserByUsername("Ernie");
	}

	@Test
	void handleUsernameTokenDisabledAccountLeavesPasswordUnset() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);
		UserDetails disabledUser = new User("Ernie", "Bert", false, true, true, true,
				Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
		when(userDetailsService.loadUserByUsername("Ernie")).thenReturn(disabledUser);

		assertDoesNotThrow(() -> this.callbackHandler.handleUsernameToken(this.passwordCallback));
		assertThat(this.passwordCallback.getPassword()).isNull();
		verify(userDetailsService).loadUserByUsername("Ernie");
	}

	@Test
	void handleUsernameTokenLockedAccountLeavesPasswordUnset() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);
		UserDetails lockedUser = new User("Ernie", "Bert", true, true, true, false,
				Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
		when(userDetailsService.loadUserByUsername("Ernie")).thenReturn(lockedUser);

		assertDoesNotThrow(() -> this.callbackHandler.handleUsernameToken(this.passwordCallback));
		assertThat(this.passwordCallback.getPassword()).isNull();
		verify(userDetailsService).loadUserByUsername("Ernie");
	}

	@Test
	void handleUsernameTokenAccountExpiredLeavesPasswordUnset() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);
		UserDetails expiredAccountUser = new User("Ernie", "Bert", true, false, true, true,
				Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
		when(userDetailsService.loadUserByUsername("Ernie")).thenReturn(expiredAccountUser);

		assertDoesNotThrow(() -> this.callbackHandler.handleUsernameToken(this.passwordCallback));
		assertThat(this.passwordCallback.getPassword()).isNull();
		verify(userDetailsService).loadUserByUsername("Ernie");
	}

	@Test
	void handleUsernameTokenCredentialsExpiredLeavesPasswordUnset() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);
		UserDetails expiredCredsUser = new User("Ernie", "Bert", true, true, false, true,
				Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
		when(userDetailsService.loadUserByUsername("Ernie")).thenReturn(expiredCredsUser);

		assertDoesNotThrow(() -> this.callbackHandler.handleUsernameToken(this.passwordCallback));
		assertThat(this.passwordCallback.getPassword()).isNull();
		verify(userDetailsService).loadUserByUsername("Ernie");
	}

	@Test
	void handleUsernameTokenPrincipalStoresAuthentication() {
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		this.callbackHandler.setUserDetailsService(userDetailsService);

		when(userDetailsService.loadUserByUsername("Ernie")).thenReturn(this.user);

		this.callbackHandler.handleUsernameTokenPrincipal(this.callback);
		SecurityContext context = SecurityContextHolder.getContext();

		assertThat(context).isNotNull();

		Authentication authentication = context.getAuthentication();

		assertThat(authentication).isNotNull();

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		assertThat(authorities).isNotNull();
		assertThat(authorities).isNotEmpty();
		assertThat(authorities.iterator().next()).isEqualTo(this.grantedAuthority);

		assertThat(authentication.getDetails()).isEqualTo(this.user);

		verify(userDetailsService).loadUserByUsername("Ernie");
	}

}
