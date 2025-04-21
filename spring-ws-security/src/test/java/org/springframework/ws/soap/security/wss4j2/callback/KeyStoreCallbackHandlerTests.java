/*
 * Copyright 2005-2025 the original author or authors.
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

import java.security.KeyStore;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

class KeyStoreCallbackHandlerTests {

	private KeyStoreCallbackHandler callbackHandler;

	private WSPasswordCallback callback;

	@BeforeEach
	void setUp() throws Exception {

		this.callbackHandler = new KeyStoreCallbackHandler();
		this.callback = new WSPasswordCallback("secretkey", WSPasswordCallback.SECRET_KEY);

		KeyStoreFactoryBean factory = new KeyStoreFactoryBean();
		factory.setLocation(new ClassPathResource("private.jks"));
		factory.setPassword("123456");
		factory.setType("JCEKS");
		factory.afterPropertiesSet();
		KeyStore keyStore = factory.getObject();
		this.callbackHandler.setKeyStore(keyStore);
		this.callbackHandler.setSymmetricKeyPassword("123456");
	}

	@Test
	void testHandleKeyName() throws Exception {

		this.callbackHandler.handleInternal(this.callback);

		assertThat(this.callback.getKey()).isNotNull();
	}

}
