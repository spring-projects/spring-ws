/*
 * Copyright 2012 Broadcom Inc. and/or its subsidiaries. All Rights Reserved.
 * Copyright 2012-present the original author or authors.
 */

package org.springframework.ws.soap.security.x509;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.ws.soap.security.x509.cache.NullX509UserCache;
import org.springframework.ws.soap.security.x509.cache.X509UserCache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link X509AuthenticationProvider}.
 *
 * @author Stephane Nicoll
 */
class X509AuthenticationProviderTest {

	@Test
	void x509AuthoritiesPopulatorIsRequired() {
		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		assertThatThrownBy(provider::afterPropertiesSet).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("X509AuthoritiesPopulator");
	}

	@Test
	void supportsX509AuthenticationToken() {
		X509AuthenticationProvider provider = createTestProvider();
		assertThat(provider.supports(X509AuthenticationToken.class)).isTrue();
	}

	@Test
	void doesNotSupportOtherTokenTypes() {
		X509AuthenticationProvider provider = createTestProvider();
		assertThat(provider.supports(TestingAuthenticationToken.class)).isFalse();
	}

	@Test
	void authenticateUnsupportedTokenReturnsNull() throws Exception {
		X509AuthenticationProvider provider = createTestProvider();
		provider.afterPropertiesSet();
		Authentication result = provider
			.authenticate(new TestingAuthenticationToken("u", "c", Collections.emptyList()));
		assertThat(result).isNull();
	}

	@Test
	void authenticateNullCertificateThrowsBadCredentials() throws Exception {
		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.afterPropertiesSet();

		X509AuthenticationToken request = new X509AuthenticationToken(null);

		assertThatThrownBy(() -> provider.authenticate(request)).isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("Certificate is null");

		verify(populator, never()).getUserDetails(any());
	}

	@Test
	void authenticateResolvesUserAndReturnsAuthenticatedToken() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("alice")
			.password("{noop}x")
			.authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
			.build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.afterPropertiesSet();

		Object details = "request-details";
		X509AuthenticationToken request = new X509AuthenticationToken(certificate);
		request.setDetails(details);

		Authentication authentication = provider.authenticate(request);

		assertThat(authentication).isInstanceOf(X509AuthenticationToken.class);
		X509AuthenticationToken token = (X509AuthenticationToken) authentication;
		assertThat(token.isAuthenticated()).isTrue();
		assertThat(token.getPrincipal()).isSameAs(user);
		assertThat(token.getCredentials()).isSameAs(certificate);
		assertThat(token.getAuthorities()).containsExactlyElementsOf(user.getAuthorities());
		assertThat(token.getDetails()).isEqualTo(details);

		verify(populator).getUserDetails(certificate);
	}

	@Test
	void authenticateUsesCacheAndCallsPopulatorOnce() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("bob").password("{noop}x").roles("ADMIN").build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		X509UserCache cache = mock(X509UserCache.class);
		when(cache.getUserFromCache(eq(certificate))).thenReturn(null, user);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.setX509UserCache(cache);
		provider.afterPropertiesSet();

		X509AuthenticationToken request = new X509AuthenticationToken(certificate);
		provider.authenticate(request);
		provider.authenticate(request);

		verify(cache, times(2)).getUserFromCache(certificate);
		verify(cache, times(1)).putUserInCache(eq(certificate), eq(user));
		verify(populator, times(1)).getUserDetails(certificate);
	}

	@Test
	void populatorExceptionPropagates() throws Exception {
		X509Certificate certificate = loadCertificate();
		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenThrow(new BadCredentialsException("rejected"));

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.setX509UserCache(new NullX509UserCache());
		provider.afterPropertiesSet();

		X509AuthenticationToken request = new X509AuthenticationToken(certificate);

		assertThatThrownBy(() -> provider.authenticate(request)).isInstanceOf(BadCredentialsException.class)
			.hasMessage("rejected");

		verify(populator).getUserDetails(certificate);
	}

	@Test
	void authenticateRejectsLockedUserFromPopulator() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("alice").password("{noop}x").roles("USER").accountLocked(true).build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.afterPropertiesSet();

		assertThatThrownBy(() -> provider.authenticate(new X509AuthenticationToken(certificate)))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bad credentials");

		verify(populator).getUserDetails(certificate);
	}

	@Test
	void authenticateRejectsDisabledUserFromPopulator() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("alice").password("{noop}x").roles("USER").disabled(true).build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		X509UserCache cache = mock(X509UserCache.class);
		when(cache.getUserFromCache(eq(certificate))).thenReturn(null);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.setX509UserCache(cache);
		provider.afterPropertiesSet();

		assertThatThrownBy(() -> provider.authenticate(new X509AuthenticationToken(certificate)))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bad credentials");

		verify(cache).getUserFromCache(certificate);
		verify(cache, never()).putUserInCache(any(), any());
		verify(populator).getUserDetails(certificate);
	}

	@Test
	void authenticateRejectsExpiredAccountFromPopulator() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("alice").password("{noop}x").roles("USER").accountExpired(true).build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.afterPropertiesSet();

		assertThatThrownBy(() -> provider.authenticate(new X509AuthenticationToken(certificate)))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bad credentials");

		verify(populator).getUserDetails(certificate);
	}

	@Test
	void authenticateRejectsExpiredCredentialsFromPopulator() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("alice")
			.password("{noop}x")
			.roles("USER")
			.credentialsExpired(true)
			.build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.afterPropertiesSet();

		assertThatThrownBy(() -> provider.authenticate(new X509AuthenticationToken(certificate)))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bad credentials");

		verify(populator).getUserDetails(certificate);
	}

	@Test
	void customAccountStatusCheckerIsInvoked() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails user = User.withUsername("alice").password("{noop}x").roles("USER").build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		when(populator.getUserDetails(eq(certificate))).thenReturn(user);

		UserDetailsChecker checker = (userDetails) -> {
			throw new DisabledException("from-custom-checker");
		};

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.setAccountStatusUserDetailsChecker(checker);
		provider.afterPropertiesSet();

		assertThatThrownBy(() -> provider.authenticate(new X509AuthenticationToken(certificate)))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bad credentials");

		verify(populator).getUserDetails(certificate);
	}

	@Test
	void authenticateRejectsInvalidUserReturnedFromCache() throws Exception {
		X509Certificate certificate = loadCertificate();
		UserDetails disabledUser = User.withUsername("cached").password("{noop}x").roles("USER").disabled(true).build();

		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);

		X509UserCache cache = mock(X509UserCache.class);
		when(cache.getUserFromCache(eq(certificate))).thenReturn(disabledUser);

		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		provider.setX509UserCache(cache);
		provider.afterPropertiesSet();

		assertThatThrownBy(() -> provider.authenticate(new X509AuthenticationToken(certificate)))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessage("Bad credentials");

		verify(populator, never()).getUserDetails(any());
		verify(cache, times(1)).getUserFromCache(certificate);
		verify(cache, never()).putUserInCache(any(), any());
	}

	private X509Certificate loadCertificate() throws Exception {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		try (InputStream is = new ClassPathResource("test-keystore.jks", getClass()).getInputStream()) {
			keyStore.load(is, "password".toCharArray());
		}
		return (X509Certificate) keyStore.getCertificate("alias");
	}

	private static X509AuthenticationProvider createTestProvider() {
		X509AuthoritiesPopulator populator = mock(X509AuthoritiesPopulator.class);
		X509AuthenticationProvider provider = new X509AuthenticationProvider();
		provider.setX509AuthoritiesPopulator(populator);
		return provider;
	}

}
