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

package org.springframework.ws.transport.http;

import java.time.Duration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for {@link HttpComponents5MessageSender}.
 *
 * @author Lars Uffmann
 * @author Stephane Nicoll
 */
class HttpComponents5MessageSenderTests {

	@Test
	void afterPropertiesSetShouldProperlyInitializeHttpClient() throws Exception {
		HttpComponents5MessageSender messageSender = new HttpComponents5MessageSender();
		assertThat(messageSender.getHttpClient()).isNull();
		messageSender.setConnectionTimeout(Duration.ofSeconds(1));

		messageSender.afterPropertiesSet();
		assertThat(messageSender.getHttpClient()).isNotNull();
	}

	@Test
	@Deprecated
	@SuppressWarnings("removal")
	void afterPropertiesSetShouldUseAlreadyProvidedHttpClientIfAvailableWithConstructor() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpComponents5MessageSender messageSender = new HttpComponents5MessageSender(httpClient);
		assertThatCode(() -> messageSender.setConnectionTimeout(Duration.ofSeconds(1)))
			.isInstanceOf(IllegalStateException.class);
		messageSender.afterPropertiesSet();
		assertThat(messageSender.getHttpClient()).isSameAs(httpClient);
	}

	@Test
	@Deprecated
	@SuppressWarnings("removal")
	void afterPropertiesSetShouldUseAlreadyProvidedHttpClientIfAvailableWithProperty() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpComponents5MessageSender messageSender = new HttpComponents5MessageSender();
		messageSender.setHttpClient(httpClient);
		messageSender.afterPropertiesSet();
		assertThat(messageSender.getHttpClient()).isSameAs(httpClient);
	}

}
