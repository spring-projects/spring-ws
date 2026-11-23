/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
class CompositeEndpointExceptionResolverTests {

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
		protected void logException(MessageContext messageContext, Exception ex) {
			if (this.logException) {
				throw new IllegalStateException("Log exception already called");
			}
			this.logException = true;
		}

	}

}