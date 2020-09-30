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

package org.springframework.ws.soap.security.xwss.callback.jaas;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;

public class JaasCertificateValidationCallbackHandlerTest {

	private JaasCertificateValidationCallbackHandler callbackHandler;

	private CertificateValidationCallback callback;

	@Before
	public void setUp() throws Exception {
		System.setProperty("java.security.auth.login.config", getClass().getResource("jaas.config").toString());
		callbackHandler = new JaasCertificateValidationCallbackHandler();
		callbackHandler.setLoginContextName("Certificate");
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream is = null;
		try {
			is = new ClassPathResource("/org/springframework/ws/soap/security/xwss/test-keystore.jks").getInputStream();
			keyStore.load(is, "password".toCharArray());
		} finally {
			if (is != null) {
				is.close();
			}
		}
		X509Certificate certificate = (X509Certificate) keyStore.getCertificate("alias");
		callback = new CertificateValidationCallback(certificate);
	}

	@Test
	public void testValidateCertificateValid() throws Exception {
		callbackHandler.handleInternal(callback);
		boolean authenticated = callback.getResult();
		Assert.assertTrue("Not authenticated", authenticated);
	}

}
