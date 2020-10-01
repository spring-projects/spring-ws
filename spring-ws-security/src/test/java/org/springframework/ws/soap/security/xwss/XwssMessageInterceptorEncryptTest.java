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

package org.springframework.ws.soap.security.xwss;

import static org.assertj.core.api.Assertions.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;

public class XwssMessageInterceptorEncryptTest extends AbstractXwssMessageInterceptorKeyStoreTestCase {

	@Test
	@Disabled("Does not run under JDK 1.8")
	public void encryptDefaultCertificate() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("encrypt-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(EncryptionKeyCallback.class);

				EncryptionKeyCallback keyCallback = (EncryptionKeyCallback) callback;

				assertThat(keyCallback.getRequest()).isInstanceOf(EncryptionKeyCallback.AliasX509CertificateRequest.class);

				EncryptionKeyCallback.AliasX509CertificateRequest request = (EncryptionKeyCallback.AliasX509CertificateRequest) keyCallback
						.getRequest();

				assertThat(request.getAlias()).isEqualTo("");

				request.setX509Certificate(certificate);
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathExists("SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey", result);
	}

	@Test
	@Disabled("Does not run under JDK 1.8")
	public void encryptAlias() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("encrypt-alias-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(EncryptionKeyCallback.class);

				EncryptionKeyCallback keyCallback = (EncryptionKeyCallback) callback;

				assertThat(keyCallback.getRequest()).isInstanceOf(EncryptionKeyCallback.AliasX509CertificateRequest.class);

				EncryptionKeyCallback.AliasX509CertificateRequest request = (EncryptionKeyCallback.AliasX509CertificateRequest) keyCallback
						.getRequest();

				assertThat(request.getAlias()).isEqualTo("alias");

				request.setX509Certificate(certificate);
			}
		};
		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathExists("SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey", result);
	}

	@Test
	@Disabled("Does not run under JDK 1.8")
	public void testDecrypt() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("decrypt-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(DecryptionKeyCallback.class);

				DecryptionKeyCallback keyCallback = (DecryptionKeyCallback) callback;

				assertThat(keyCallback.getRequest()).isInstanceOf(DecryptionKeyCallback.X509CertificateBasedRequest.class);

				DecryptionKeyCallback.X509CertificateBasedRequest request = (DecryptionKeyCallback.X509CertificateBasedRequest) keyCallback
						.getRequest();

				assertThat(request.getX509Certificate()).isEqualTo(certificate);

				request.setPrivateKey(privateKey);
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("encrypted-soap.xml");
		interceptor.validateMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathNotExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
	}
}
