/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ws.client.support.interceptor;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;

/**
 * @author Greg Turnquist
 */
public class ClientInterceptorAdapterTest {

	@Test
	public void handleEmptyInterceptor() {

		ClientInterceptor interceptor = new ClientInterceptorAdapter() {};

		assertThat(interceptor.handleRequest(null)).isTrue();
		assertThat(interceptor.handleResponse(null)).isTrue();
		assertThat(interceptor.handleFault(null)).isTrue();

		interceptor.afterCompletion(null, null);
	}

	@Test
	public void handleTestAdapter() {
		TestClientInterceptorAdapter interceptor = new TestClientInterceptorAdapter(new ArrayList<>());

		assertThat(interceptor.handleRequest(null)).isFalse();
		assertThat(interceptor.handleResponse(null)).isFalse();
		assertThat(interceptor.handleFault(null)).isFalse();

		interceptor.afterCompletion(null, null);

		List<String> bits = interceptor.getBits();

		assertThat(bits).containsExactly("handled request", "handled response", "handled fault", "handled afterCompletion");
	}

	static class TestClientInterceptorAdapter extends ClientInterceptorAdapter {

		private final List<String> bits;

		public TestClientInterceptorAdapter(List<String> bits) {
			this.bits = bits;
		}

		public List<String> getBits() {
			return bits;
		}

		@Override
		public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {

			bits.add("handled request");
			return false;
		}

		@Override
		public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {

			bits.add("handled response");
			return false;
		}

		@Override
		public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {

			bits.add("handled fault");
			return false;
		}

		@Override
		public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
			bits.add("handled afterCompletion");
		}
	}
}
