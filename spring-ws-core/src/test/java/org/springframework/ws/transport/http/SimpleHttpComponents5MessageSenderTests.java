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