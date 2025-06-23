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

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.ws.transport.http.HttpComponents5ClientFactory.HttpClientBuilderCustomizer;
import org.springframework.ws.transport.http.HttpComponents5ClientFactory.PoolingHttpClientConnectionManagerBuilderCustomizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link HttpComponents5ClientFactory}.
 *
 * @author Stephane Nicoll
 */
class HttpComponents5ClientFactoryTests {

	@Test
	void httpclientBuilderCustomizersAreCalledInOrder() {
		HttpClientBuilderCustomizer first = mock(HttpClientBuilderCustomizer.class);
		HttpClientBuilderCustomizer second = mock(HttpClientBuilderCustomizer.class);
		HttpComponents5ClientFactory factory = new HttpComponents5ClientFactory();
		factory.addClientBuilderCustomizer(first);
		factory.addClientBuilderCustomizer(second);
		assertThat(factory.build()).isNotNull();
		InOrder inOrder = inOrder(first, second);
		inOrder.verify(first).customize(any());
		inOrder.verify(second).customize(any());
	}

	@Test
	void connectionManagerBuilderCustomizerCustomizersAreCalledInOrder() {
		PoolingHttpClientConnectionManagerBuilderCustomizer first = mock(
				PoolingHttpClientConnectionManagerBuilderCustomizer.class);
		PoolingHttpClientConnectionManagerBuilderCustomizer second = mock(
				PoolingHttpClientConnectionManagerBuilderCustomizer.class);
		HttpComponents5ClientFactory factory = new HttpComponents5ClientFactory();
		factory.addConnectionManagerBuilderCustomizer(first);
		factory.addConnectionManagerBuilderCustomizer(second);
		assertThat(factory.build()).isNotNull();
		InOrder inOrder = inOrder(first, second);
		inOrder.verify(first).customize(any());
		inOrder.verify(second).customize(any());
	}

}