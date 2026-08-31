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

package org.springframework.ws.server.endpoint.mapping;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.SmartEndpointInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractEndpointMapping}.
 *
 * @author Stephane Nicoll
 */
class AbstractEndpointMappingTests {

	@Test
	void smartInterceptorsAreSortedByOrderRatherThanRegistrationOrder() throws Exception {
		EndpointInvocationChain chain = chainFor(new OrderedSmartInterceptor("last", Ordered.LOWEST_PRECEDENCE),
				new OrderedSmartInterceptor("first", Ordered.HIGHEST_PRECEDENCE));
		assertThat(names(chain)).containsExactly("first", "last");
	}

	@Test
	void smartInterceptorsThatAreNotOrderedKeepTheirRelativeOrderAndRunLast() throws Exception {
		EndpointInvocationChain chain = chainFor(new NamedSmartInterceptor("plainA"),
				new OrderedSmartInterceptor("ordered", 0), new NamedSmartInterceptor("plainB"));
		assertThat(names(chain)).containsExactly("ordered", "plainA", "plainB");
	}

	@Test
	void configuredInterceptorsKeepTheirOrderAndRunBeforeSmartInterceptors() throws Exception {
		StaticApplicationContext context = new StaticApplicationContext();
		registerBeans(context, new OrderedSmartInterceptor("smart", Ordered.HIGHEST_PRECEDENCE));
		context.refresh();

		AbstractEndpointMapping mapping = new TestEndpointMapping();
		mapping.setInterceptors(
				new EndpointInterceptor[] { new NamedInterceptor("configuredA"), new NamedInterceptor("configuredB") });
		mapping.setApplicationContext(context);
		assertThat(names(mapping.getEndpoint(messageContext()))).containsExactly("configuredA", "configuredB", "smart");
	}

	@Test
	void smartInterceptorsAreSortedByAnOrderAnnotationOnTheBeanClass() throws Exception {
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerSingleton("annotatedLast", AnnotatedLastInterceptor.class);
		context.registerSingleton("annotatedFirst", AnnotatedFirstInterceptor.class);
		context.refresh();
		AbstractEndpointMapping mapping = new TestEndpointMapping();
		mapping.setApplicationContext(context);
		assertThat(names(mapping.getEndpoint(messageContext()))).containsExactly("annotatedFirst", "annotatedLast");
	}

	@Order(-100)
	public static class AnnotatedFirstInterceptor extends NamedSmartInterceptor {

		public AnnotatedFirstInterceptor() {
			super("annotatedFirst");
		}

	}

	@Order(100)
	public static class AnnotatedLastInterceptor extends NamedSmartInterceptor {

		public AnnotatedLastInterceptor() {
			super("annotatedLast");
		}

	}

	private static EndpointInvocationChain chainFor(SmartEndpointInterceptor... interceptors) throws Exception {
		StaticApplicationContext context = new StaticApplicationContext();
		registerBeans(context, interceptors);
		context.refresh();

		AbstractEndpointMapping mapping = new TestEndpointMapping();
		mapping.setApplicationContext(context);
		return mapping.getEndpoint(messageContext());
	}

	private static void registerBeans(StaticApplicationContext context, SmartEndpointInterceptor... interceptors) {
		for (int i = 0; i < interceptors.length; i++) {
			context.getBeanFactory().registerSingleton("interceptor" + i, interceptors[i]);
		}
	}

	private static MessageContext messageContext() {
		return new DefaultMessageContext(new MockWebServiceMessage("<root/>"), new MockWebServiceMessageFactory());
	}

	private static List<String> names(EndpointInvocationChain chain) {
		return Arrays.stream(chain.getInterceptors()).map(Object::toString).toList();
	}

	private static class NamedInterceptor implements EndpointInterceptor {

		private final String name;

		NamedInterceptor(String name) {
			this.name = name;
		}

		@Override
		public boolean handleRequest(MessageContext messageContext, Object endpoint) {
			return true;
		}

		@Override
		public boolean handleResponse(MessageContext messageContext, Object endpoint) {
			return true;
		}

		@Override
		public boolean handleFault(MessageContext messageContext, Object endpoint) {
			return true;
		}

		@Override
		public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
		}

		@Override
		public String toString() {
			return this.name;
		}

	}

	private static class NamedSmartInterceptor extends NamedInterceptor implements SmartEndpointInterceptor {

		NamedSmartInterceptor(String name) {
			super(name);
		}

		@Override
		public boolean shouldIntercept(MessageContext messageContext, Object endpoint) {
			return true;
		}

	}

	private static final class OrderedSmartInterceptor extends NamedSmartInterceptor implements Ordered {

		private final int order;

		OrderedSmartInterceptor(String name, int order) {
			super(name);
			this.order = order;
		}

		@Override
		public int getOrder() {
			return this.order;
		}

	}

	private static class TestEndpointMapping extends AbstractEndpointMapping {

		@Override
		protected @Nullable Object getEndpointInternal(MessageContext messageContext) {
			return new Object();
		}

	}

}
