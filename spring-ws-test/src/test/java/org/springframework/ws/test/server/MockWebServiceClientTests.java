/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

import static org.assertj.core.api.Assertions.assertThat;

class MockWebServiceClientTests {

	@Test
	void createServerApplicationContext() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("messageDispatcher", SoapMessageDispatcher.class);
		applicationContext.registerSingleton("messageFactory", SaajSoapMessageFactory.class);
		applicationContext.refresh();

		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);

		assertThat(client).isNotNull();
	}

	@Test
	void createServerApplicationContextDefaults() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.refresh();

		MockWebServiceClient client = MockWebServiceClient.createClient(applicationContext);

		assertThat(client).isNotNull();
	}

}
