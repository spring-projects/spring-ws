/*
 * Copyright 2005-2022 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.x509.X509AuthenticationToken;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;

public class SpringCertificateValidationCallbackHandlerTest {

	private SpringCertificateValidationCallbackHandler callbackHandler;

	private AuthenticationManager authenticationManager;

	private X509Certificate certificate;

	private CertificateValidationCallback callback;

	@BeforeEach
	public void setUp() throws Exception {

		callbackHandler = new SpringCertificateValidationCallbackHandler();
		authenticationManager = createMock(AuthenticationManager.class);
		callbackHandler.setAuthenticationManager(authenticationManager);
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

		try (InputStream is = new ClassPathResource("/org/springframework/ws/soap/security/xwss/test-keystore.jks")
				.getInputStream()) {
			keyStore.load(is, "password".toCharArray());
		}

		certificate = (X509Certificate) keyStore.getCertificate("alias");
		callback = new CertificateValidationCallback(certificate);
	}

	@AfterEach
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testValidateCertificateValid() throws Exception {

		expect(authenticationManager.authenticate(isA(X509AuthenticationToken.class)))
				.andReturn(new TestingAuthenticationToken(certificate, null, Collections.<GrantedAuthority> emptyList()));

		replay(authenticationManager);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isTrue();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

		verify(authenticationManager);
	}

	@Test
	public void testValidateCertificateInvalid() throws Exception {

		expect(authenticationManager.authenticate(isA(X509AuthenticationToken.class)))
				.andThrow(new BadCredentialsException(""));

		replay(authenticationManager);

		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();

		assertThat(authenticated).isFalse();
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

		verify(authenticationManager);
	}

	@Test
	public void testCleanUp() throws Exception {

		TestingAuthenticationToken authentication = new TestingAuthenticationToken(new Object(), new Object(),
				Collections.<GrantedAuthority> emptyList());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		CleanupCallback cleanupCallback = new CleanupCallback();
		callbackHandler.handleInternal(cleanupCallback);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}
}
