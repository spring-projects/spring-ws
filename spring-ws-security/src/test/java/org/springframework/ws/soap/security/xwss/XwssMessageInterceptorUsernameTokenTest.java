/*
 * Copyright 2005-2022 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

public class XwssMessageInterceptorUsernameTokenTest extends AbstractXwssMessageInterceptorTestCase {

	@Test
	public void testAddUsernameTokenDigest() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("usernameToken-digest-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOfAny(UsernameCallback.class, PasswordCallback.class);

				if (callback instanceof UsernameCallback) {
					((UsernameCallback) callback).setUsername("Bert");
				} else if (callback instanceof PasswordCallback) {
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					passwordCallback.setPassword("Ernie");
				}
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathEvaluatesTo("Bert",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
		assertXpathExists(
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest']",
				result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Nonce", result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsu:Created", result);
	}

	@Test
	public void testAddUsernameTokenPlainText() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("usernameToken-plainText-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOfAny(UsernameCallback.class, PasswordCallback.class);

				if (callback instanceof UsernameCallback) {
					((UsernameCallback) callback).setUsername("Bert");
				} else if (callback instanceof PasswordCallback) {
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					passwordCallback.setPassword("Ernie");
				}
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathEvaluatesTo("Bert",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
		assertXpathEvaluatesTo("Ernie",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
				result);
	}

	@Test
	public void testAddUsernameTokenPlainTextNonce() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("usernameToken-plainText-nonce-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOfAny(UsernameCallback.class, PasswordCallback.class);

				if (callback instanceof UsernameCallback) {
					((UsernameCallback) callback).setUsername("Bert");
				} else if (callback instanceof PasswordCallback) {
					PasswordCallback passwordCallback = (PasswordCallback) callback;
					passwordCallback.setPassword("Ernie");
				}
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("empty-soap.xml");
		interceptor.secureMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathEvaluatesTo("Bert",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Username/text()", result);
		assertXpathEvaluatesTo("Ernie",
				"/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']/text()",
				result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsse:Nonce", result);
		assertXpathExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/wsse:UsernameToken/wsu:Created", result);
	}

	@Test
	public void testValidateUsernameTokenPlainText() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("requireUsernameToken-plainText-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOf(PasswordValidationCallback.class);

				PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
				validationCallback.setValidator(request -> {

					assertThat(request).isInstanceOf(PasswordValidationCallback.PlainTextPasswordRequest.class);

					PasswordValidationCallback.PlainTextPasswordRequest passwordRequest = (PasswordValidationCallback.PlainTextPasswordRequest) request;

					assertThat(passwordRequest.getUsername()).isEqualTo("Bert");
					assertThat(passwordRequest.getPassword()).isEqualTo("Ernie");

					return true;
				});
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("usernameTokenPlainText-soap.xml");
		interceptor.validateMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathNotExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
	}

	@Test
	public void testValidateUsernameTokenPlainTextNonce() throws Exception {

		interceptor
				.setPolicyConfiguration(new ClassPathResource("requireUsernameToken-plainText-nonce-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOfAny(PasswordValidationCallback.class, TimestampValidationCallback.class);

				if (callback instanceof PasswordValidationCallback) {

					PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
					validationCallback.setValidator(request -> {

						assertThat(request).isInstanceOf(PasswordValidationCallback.PlainTextPasswordRequest.class);

						PasswordValidationCallback.PlainTextPasswordRequest passwordRequest = (PasswordValidationCallback.PlainTextPasswordRequest) request;

						assertThat(passwordRequest.getUsername()).isEqualTo("Bert");
						assertThat(passwordRequest.getPassword()).isEqualTo("Ernie");

						return true;
					});
				} else if (callback instanceof TimestampValidationCallback) {

					TimestampValidationCallback validationCallback = (TimestampValidationCallback) callback;
					validationCallback.setValidator(request -> {});
				}
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("usernameTokenPlainText-nonce-soap.xml");
		interceptor.validateMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathNotExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
	}

	@Test
	public void testValidateUsernameTokenDigest() throws Exception {

		interceptor.setPolicyConfiguration(new ClassPathResource("requireUsernameToken-digest-config.xml", getClass()));

		CallbackHandler handler = new AbstractCallbackHandler() {

			@Override
			protected void handleInternal(Callback callback) {

				assertThat(callback).isInstanceOfAny(PasswordValidationCallback.class, TimestampValidationCallback.class);

				if (callback instanceof PasswordValidationCallback) {

					PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;

					assertThat(validationCallback.getRequest())
							.isInstanceOf(PasswordValidationCallback.DigestPasswordRequest.class);

					PasswordValidationCallback.DigestPasswordRequest passwordRequest = (PasswordValidationCallback.DigestPasswordRequest) validationCallback
							.getRequest();

					assertThat(passwordRequest.getUsername()).isEqualTo("Bert");

					passwordRequest.setPassword("Ernie");
					validationCallback.setValidator(new PasswordValidationCallback.DigestPasswordValidator());
				} else if (callback instanceof TimestampValidationCallback) {

					TimestampValidationCallback validationCallback = (TimestampValidationCallback) callback;
					validationCallback.setValidator(request -> {});
				}
			}
		};

		interceptor.setCallbackHandler(handler);
		interceptor.afterPropertiesSet();
		SaajSoapMessage message = loadSaajMessage("usernameTokenDigest-soap.xml");
		interceptor.validateMessage(message, null);
		SOAPMessage result = message.getSaajMessage();

		assertThat(result).isNotNull();
		assertXpathNotExists("/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security", result);
	}
}
