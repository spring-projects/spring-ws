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

package org.springframework.ws.server.test;

import org.springframework.core.Ordered;
import org.springframework.ws.server.SmartEndpointInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test {@link Ordered} {@link SmartEndpointInterceptor}.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public interface OrderedSmartEndpointInterceptor extends SmartEndpointInterceptor, Ordered {

	/**
	 * Create a mock {@link SmartEndpointInterceptor} that implements {@link Ordered} with
	 * the given order, indicating whether the interceptor should intercept the request.
	 * @param order the order
	 * @param shouldIntercept whether the interceptor should intercept the request
	 * @return a mock
	 */
	static SmartEndpointInterceptor mockInterceptor(int order, boolean shouldIntercept) {
		OrderedSmartEndpointInterceptor interceptor = mock(OrderedSmartEndpointInterceptor.class);
		given(interceptor.getOrder()).willReturn(order);
		given(interceptor.shouldIntercept(any(), any())).willReturn(shouldIntercept);
		return interceptor;
	}

}
