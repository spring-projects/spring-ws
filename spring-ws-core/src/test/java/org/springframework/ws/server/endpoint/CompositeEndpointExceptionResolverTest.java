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

package org.springframework.ws.server.endpoint;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ws.context.MessageContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CompositeEndpointExceptionResolver}.
 *
 * @author Stephane Nicoll
 */
class CompositeEndpointExceptionResolverTest {

	@Test
	void logExceptionIsInvokedOnlyOnResolvedInstance() {
		TestExceptionResolver first = new TestExceptionResolver(false);
		TestExceptionResolver second = new TestExceptionResolver(true);
		TestExceptionResolver third = new TestExceptionResolver(false);
		CompositeEndpointExceptionResolver resolver = new CompositeEndpointExceptionResolver(
				List.of(first, second, third));
		boolean resolved = resolver.resolveException(null, null, new RuntimeException("test"));
		assertThat(resolved).isTrue();
		assertThat(first.logException).isFalse();
		assertThat(second.logException).isTrue();
		assertThat(third.logException).isFalse();
	}

	@Test
	void logExceptionIsInvokedOnLastResolvedInstanceIfNecessary() {
		TestExceptionResolver first = new TestExceptionResolver(false);
		TestExceptionResolver second = new TestExceptionResolver(false);
		TestExceptionResolver third = new TestExceptionResolver(false);
		CompositeEndpointExceptionResolver resolver = new CompositeEndpointExceptionResolver(
				List.of(first, second, third));
		boolean resolved = resolver.resolveException(null, null, new RuntimeException("test"));
		assertThat(resolved).isFalse();
		assertThat(first.logException).isFalse();
		assertThat(second.logException).isFalse();
		assertThat(third.logException).isTrue();
	}

	public static class TestExceptionResolver extends AbstractEndpointExceptionResolver {

		private final boolean resolve;

		private boolean logException;

		public TestExceptionResolver(boolean resolve) {
			this.resolve = resolve;
		}

		@Override
		protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
			return this.resolve;
		}

		@Override
		protected void logException(Exception ex, MessageContext messageContext) {
			if (this.logException) {
				throw new IllegalStateException("Log exception already called");
			}
			this.logException = true;
		}

	}

}