/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.xwss;

import static org.assertj.core.api.Assertions.*;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;

public class XwssMessageInterceptorSignTest extends AbstractXwssMessageInterceptorKeyStoreTestCase {

	@Test
	public void testSignDefaultCertificate() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("sign-config.xml", getClass()));
		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(SignatureKeyCallback.class);

				SignatureKeyCallback keyCallback = (SignatureKeyCallback) callback;

				assertThat(keyCallback.getRequest()).isInstanceOf(SignatureKeyCallback.DefaultPrivKeyCertRequest.class);

				SignatureKeyCallback.DefaultPrivKeyCertRequest request = (SignatureKeyCallback.DefaultPrivKeyCertRequest) keyCallback
						.getRequest();
				request.setX509Certificate(certificate);
				request.setPrivateKey(privateKey);
			}
		};
		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathExists("SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature", result);
	}

	@Test
	public void testSignAlias() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("sign-alias-config.xml", getClass()));
		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(SignatureKeyCallback.class);

				SignatureKeyCallback keyCallback = (SignatureKeyCallback) callback;

				assertThat(keyCallback.getRequest()).isInstanceOf(SignatureKeyCallback.AliasPrivKeyCertRequest.class);

				SignatureKeyCallback.AliasPrivKeyCertRequest request = (SignatureKeyCallback.AliasPrivKeyCertRequest) keyCallback
						.getRequest();

				assertThat(request.getAlias()).isEqualTo("alias");

				request.setX509Certificate(certificate);
				request.setPrivateKey(privateKey);
			}
		};
		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathExists("SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:BinarySecurityToken", result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/ds:Signature", result);
	}

	@Test
	public void testValidateCertificate() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("requireSignature-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(CertificateValidationCallback.class);

				CertificateValidationCallback validationCallback = (CertificateValidationCallback) callback;

				validationCallback.setValidator(passedCertificate -> {

					assertThat(passedCertificate).isEqualTo(certificate);
					return true;
				});
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("signed-soap.xml");
		interceptor.validateMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathNotExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
	}
}
