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

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

public abstract class Wss4jMessageInterceptorEncryptionTests extends Wss4jTests {

	protected Wss4jSecurityInterceptor interceptor;

	@Override
	protected void onSetup() throws Exception {

		this.interceptor = new Wss4jSecurityInterceptor();
		this.interceptor.setValidationActions("Encrypt");
		this.interceptor.setSecurementActions("Encrypt");

		KeyStoreCallbackHandler callbackHandler = new KeyStoreCallbackHandler();
		callbackHandler.setPrivateKeyPassword("123456");
		this.interceptor.setValidationCallbackHandler(callbackHandler);

		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();

		Properties cryptoFactoryBeanConfig = new Properties();
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.provider",
				"org.apache.ws.security.components.crypto.Merlin");
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jceks");
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "123456");

		// from the class path
		cryptoFactoryBeanConfig.setProperty("org.apache.ws.security.crypto.merlin.file", "private.jks");
		cryptoFactoryBean.setConfiguration(cryptoFactoryBeanConfig);
		cryptoFactoryBean.afterPropertiesSet();
		this.interceptor.setValidationDecryptionCrypto(cryptoFactoryBean.getObject());
		this.interceptor.setSecurementEncryptionCrypto(cryptoFactoryBean.getObject());

		this.interceptor.afterPropertiesSet();
	}

	@Test
	void testDecryptRequest() throws Exception {

		SoapMessage message = loadSoap11Message("encrypted-soap.xml");
		MessageContext messageContext = new DefaultMessageContext(message, getSoap11MessageFactory());
		this.interceptor.validateMessage(message, messageContext);
		Document document = getDocument((SoapMessage) messageContext.getRequest());

		assertXpathEvaluatesTo("Decryption error", "Hello", "/SOAP-ENV:Envelope/SOAP-ENV:Body/echo:echoRequest/text()",
				document);
		assertXpathNotExists("Security Header not removed", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security",
				getDocument(message));
	}

	@Test
	void testEncryptResponse() throws Exception {

		SoapMessage message = loadSoap11Message("empty-soap.xml");
		MessageContext messageContext = getSoap11MessageContext(message);
		this.interceptor.setSecurementEncryptionUser("rsakey");
		this.interceptor.secureMessage(message, messageContext);
		Document document = getDocument(message);

		assertXpathExists("Encryption error", "/SOAP-ENV:Envelope/SOAP-ENV:Header/wsse:Security/xenc:EncryptedKey",
				document);
	}

}
