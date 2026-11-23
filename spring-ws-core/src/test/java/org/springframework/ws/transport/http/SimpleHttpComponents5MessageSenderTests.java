/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.net.URI;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpComponents5ClientFactory.HttpClientBuilderCustomizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SimpleHttpComponents5MessageSender}.
 */
class SimpleHttpComponents5MessageSenderTests {

	@Test
	void createExposesHttpClient() {
		HttpClient httpClient = HttpClientBuilder.create().build();
		SimpleHttpComponents5MessageSender messageSender = new SimpleHttpComponents5MessageSender(httpClient);
		assertThat(messageSender.getHttpClient()).isSameAs(httpClient);
	}

	@Test
	void createWithNullFails() {
		HttpClient httpClient = null;
		assertThatIllegalArgumentException().isThrownBy(() -> new SimpleHttpComponents5MessageSender(httpClient));
	}

	@Test
	void createWithFactory() {
		HttpComponents5ClientFactory factory = new HttpComponents5ClientFactory();
		HttpClientBuilderCustomizer builderCustomizer = mock(HttpClientBuilderCustomizer.class);
		factory.addClientBuilderCustomizer(builderCustomizer);
		new SimpleHttpComponents5MessageSender(factory);
		verify(builderCustomizer).customize(any());
	}

	@Test
	void createConnectionUsesHttpClient() throws IOException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		SimpleHttpComponents5MessageSender messageSender = new SimpleHttpComponents5MessageSender(httpClient);
		WebServiceConnection webServiceConnection = messageSender
			.createConnection(URI.create("https://ws.example.com"));
		assertThat(webServiceConnection).isInstanceOfSatisfying(HttpComponents5Connection.class,
				(connection) -> assertThat(connection).hasFieldOrPropertyWithValue("httpClient", httpClient));
	}

}