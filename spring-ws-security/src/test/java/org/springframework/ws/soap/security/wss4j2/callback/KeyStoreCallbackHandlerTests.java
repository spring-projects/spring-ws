/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
