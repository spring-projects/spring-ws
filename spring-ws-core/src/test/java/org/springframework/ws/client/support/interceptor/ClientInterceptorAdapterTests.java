/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Greg Turnquist
 */
class ClientInterceptorAdapterTests {

	@Test
	void handleEmptyInterceptor() {

		ClientInterceptor interceptor = new ClientInterceptorAdapter() {
		};

		assertThat(interceptor.handleRequest(null)).isTrue();
		assertThat(interceptor.handleResponse(null)).isTrue();
		assertThat(interceptor.handleFault(null)).isTrue();

		interceptor.afterCompletion(null, null);
	}

	@Test
	void handleTestAdapter() {
		TestClientInterceptorAdapter interceptor = new TestClientInterceptorAdapter(new ArrayList<>());

		assertThat(interceptor.handleRequest(null)).isFalse();
		assertThat(interceptor.handleResponse(null)).isFalse();
		assertThat(interceptor.handleFault(null)).isFalse();

		interceptor.afterCompletion(null, null);

		List<String> bits = interceptor.getBits();

		assertThat(bits).containsExactly("handled request", "handled response", "handled fault",
				"handled afterCompletion");
	}

	static class TestClientInterceptorAdapter extends ClientInterceptorAdapter {

		private final List<String> bits;

		public TestClientInterceptorAdapter(List<String> bits) {
			this.bits = bits;
		}

		public List<String> getBits() {
			return this.bits;
		}

		@Override
		public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {

			this.bits.add("handled request");
			return false;
		}

		@Override
		public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {

			this.bits.add("handled response");
			return false;
		}

		@Override
		public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {

			this.bits.add("handled fault");
			return false;
		}

		@Override
		public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
			this.bits.add("handled afterCompletion");
		}

	}

}
