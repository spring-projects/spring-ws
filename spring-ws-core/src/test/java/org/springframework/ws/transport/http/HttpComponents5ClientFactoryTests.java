/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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