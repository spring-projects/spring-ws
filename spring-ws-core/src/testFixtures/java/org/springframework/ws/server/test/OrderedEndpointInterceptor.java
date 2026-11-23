/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.test;

import org.springframework.core.Ordered;
import org.springframework.ws.server.EndpointInterceptor;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test {@link Ordered} {@link EndpointInterceptor}.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public interface OrderedEndpointInterceptor extends EndpointInterceptor, Ordered {

	/**
	 * Create a mock {@link EndpointInterceptor} that implements {@link Ordered} with the
	 * given order.
	 * @param order the order
	 * @return a mock
	 */
	static EndpointInterceptor mockInterceptor(int order) {
		OrderedEndpointInterceptor interceptor = mock(OrderedEndpointInterceptor.class);
		given(interceptor.getOrder()).willReturn(order);
		return interceptor;
	}

}
