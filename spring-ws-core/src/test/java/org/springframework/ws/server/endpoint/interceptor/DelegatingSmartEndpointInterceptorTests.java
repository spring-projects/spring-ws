/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.interceptor;

import org.junit.jupiter.api.Test;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.test.OrderedEndpointInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DelegatingSmartEndpointInterceptor}.
 *
 * @author Stephane Nicoll
 */
class DelegatingSmartEndpointInterceptorTests {

	@Test
	void orderIsSetToDefaultIfTargetDoesNotImplementOrdered() {
		EndpointInterceptor endpointInterceptor = mock(EndpointInterceptor.class);
		DelegatingSmartEndpointInterceptor interceptor = new DelegatingSmartEndpointInterceptor(endpointInterceptor);
		assertThat(interceptor.getOrder()).isEqualTo(DelegatingSmartEndpointInterceptor.DEFAULT_ORDER);
	}

	@Test
	void orderIsSetToTargetValueIfTargetImplementsOrdered() {
		EndpointInterceptor endpointInterceptor = OrderedEndpointInterceptor.mockInterceptor(42);
		DelegatingSmartEndpointInterceptor interceptor = new DelegatingSmartEndpointInterceptor(endpointInterceptor);
		assertThat(interceptor.getOrder()).isEqualTo(42);
	}

}
