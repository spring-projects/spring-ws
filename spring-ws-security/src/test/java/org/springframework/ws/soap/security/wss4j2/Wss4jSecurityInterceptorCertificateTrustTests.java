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

package org.springframework.ws.soap.security.wss4j2;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for {@link Wss4jSecurityInterceptor#verifyCertificateTrust(WSHandlerResult)},
 * ensuring the trust of every signing certificate is verified.
 *
 * @author Stephane Nicoll
 */
class Wss4jSecurityInterceptorCertificateTrustTests {

	private Wss4jSecurityInterceptor interceptor;

	private X509Certificate trustedCertificate;

	private X509Certificate untrustedCertificate;

	@BeforeAll
	static void initializeSecurityLibrary() {
		// verifyCertificateTrust is exercised directly, without going through
		// processSecurityHeader, so bootstrap the WSS4J/xml-security library explicitly.
		WSSConfig.init();
	}

	@BeforeEach
	void setUp() throws Exception {
		this.interceptor = new Wss4jSecurityInterceptor();
		this.interceptor.setValidationActions("Signature");
		this.interceptor.setValidationSignatureCrypto(createCrypto());
		this.interceptor.afterPropertiesSet();
		this.trustedCertificate = loadCertificate("private.jks", "jceks", "123456", "rsakey");
		this.untrustedCertificate = loadCertificate("org/springframework/ws/soap/security/x509/test-keystore.jks",
				"jks", "password", "alias");
	}

	@Test
	void trustIsVerifiedForASingleCertificate() {
		assertThatNoException()
			.isThrownBy(() -> this.interceptor.verifyCertificateTrust(signResult(this.trustedCertificate)));
	}

	@Test
	void trustIsVerifiedForEverySigningCertificate() {
		assertThatExceptionOfType(WSSecurityException.class)
			.isThrownBy(() -> this.interceptor
				.verifyCertificateTrust(signResult(this.trustedCertificate, this.untrustedCertificate)))
			.withMessageContaining("Error during certificate path validation");
	}

	private WSHandlerResult signResult(X509Certificate... certificates) {
		List<WSSecurityEngineResult> results = new ArrayList<>();
		for (X509Certificate certificate : certificates) {
			WSSecurityEngineResult result = new WSSecurityEngineResult(WSConstants.SIGN);
			result.put(WSSecurityEngineResult.TAG_X509_CERTIFICATE, certificate);
			results.add(result);
		}
		Map<Integer, List<WSSecurityEngineResult>> actionResults = new HashMap<>();
		actionResults.put(WSConstants.SIGN, results);
		return new WSHandlerResult("", results, actionResults);
	}

	private static Crypto createCrypto() throws Exception {
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setCryptoProvider(Merlin.class);
		cryptoFactoryBean.setKeyStoreType("jceks");
		cryptoFactoryBean.setKeyStorePassword("123456");
		cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));
		cryptoFactoryBean.afterPropertiesSet();
		return cryptoFactoryBean.getObject();
	}

	private static X509Certificate loadCertificate(String location, String type, String password, String alias)
			throws Exception {
		KeyStore keyStore = KeyStore.getInstance(type);
		try (InputStream in = new ClassPathResource(location).getInputStream()) {
			keyStore.load(in, password.toCharArray());
		}
		return (X509Certificate) keyStore.getCertificate(alias);
	}

}
