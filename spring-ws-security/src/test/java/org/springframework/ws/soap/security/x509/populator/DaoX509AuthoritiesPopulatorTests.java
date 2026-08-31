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

package org.springframework.ws.soap.security.x509.populator;

import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DaoX509AuthoritiesPopulator}.
 *
 * @author Stephane Nicoll
 */
class DaoX509AuthoritiesPopulatorTests {

	private DaoX509AuthoritiesPopulator populator;

	@BeforeEach
	void setUp() throws Exception {
		this.populator = new DaoX509AuthoritiesPopulator();
		this.populator.setUserDetailsService(username -> {
			if ("jimi".equals(username)) {
				return new User("jimi", "pwd", Collections.emptyList());
			}
			throw new UsernameNotFoundException(username);
		});
		this.populator.afterPropertiesSet();
	}

	@Test
	void commonNameFollowedByOtherAttributes() {
		UserDetails user = this.populator.getUserDetails(certificateWithSubject("CN=jimi,OU=eng,O=Example,C=US"));
		assertThat(user.getUsername()).isEqualTo("jimi");
	}

	@Test
	void commonNameIsTheOnlyAttribute() {
		UserDetails user = this.populator.getUserDetails(certificateWithSubject("CN=jimi"));
		assertThat(user.getUsername()).isEqualTo("jimi");
	}

	@Test
	void commonNameIsTheLastAttribute() {
		UserDetails user = this.populator.getUserDetails(certificateWithSubject("OU=eng,O=Example,CN=jimi"));
		assertThat(user.getUsername()).isEqualTo("jimi");
	}

	@Test
	void subjectWithNoCommonNameIsRejected() {
		X509Certificate certificate = certificateWithSubject("OU=eng,O=Example,C=US");
		assertThatExceptionOfType(BadCredentialsException.class)
			.isThrownBy(() -> this.populator.getUserDetails(certificate));
	}

	@Test
	void commonNameEmbeddedInAnotherAttributeValueIsNotUsed() {
		X509Certificate certificate = certificateWithSubject("OU=xCN\\=jimi,O=Example,C=US");
		assertThatExceptionOfType(BadCredentialsException.class)
			.isThrownBy(() -> this.populator.getUserDetails(certificate));
	}

	@Test
	void commonNameEmbeddedInAnotherAttributeValueDoesNotShadowRealCommonName() {
		UserDetails user = this.populator.getUserDetails(certificateWithSubject("OU=xCN\\=admin,CN=jimi,O=Example"));
		assertThat(user.getUsername()).isEqualTo("jimi");
	}

	private static X509Certificate certificateWithSubject(String subjectDN) {
		X509Certificate certificate = mock(X509Certificate.class);
		given(certificate.getSubjectX500Principal()).willReturn(new X500Principal(subjectDN));
		return certificate;
	}

}
