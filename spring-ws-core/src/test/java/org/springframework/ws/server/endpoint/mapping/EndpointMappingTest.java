/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;

/**
 * Test case for {@link AbstractEndpointMapping}.
 */
public class EndpointMappingTest {

	private MessageContext messageContext;

	@BeforeEach
	public void setUp() throws Exception {
		messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@Test
	public void defaultEndpoint() throws Exception {

		Object defaultEndpoint = new Object();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertThat(givenRequest).isEqualTo(messageContext);
				return null;
			}
		};
		mapping.setDefaultEndpoint(defaultEndpoint);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result).isNotNull();
		assertThat(result.getEndpoint()).isEqualTo(defaultEndpoint);
	}

	@Test
	public void endpoint() throws Exception {

		final Object endpoint = new Object();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertThat(givenRequest).isEqualTo(messageContext);
				return endpoint;
			}
		};

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result).isNotNull();
		assertThat(result.getEndpoint()).isEqualTo(endpoint);
	}

	@Test
	public void endpointInterceptors() throws Exception {

		final Object endpoint = new Object();
		EndpointInterceptor interceptor = new EndpointInterceptorAdapter();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertThat(givenRequest).isEqualTo(messageContext);
				return endpoint;
			}
		};

		mapping.setInterceptors(new EndpointInterceptor[] { interceptor });
		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result.getInterceptors()).hasSize(1);
		assertThat(result.getInterceptors()[0]).isEqualTo(interceptor);
	}

	@Test
	public void smartEndpointInterceptors() throws Exception {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("smartInterceptor", MySmartEndpointInterceptor.class);

		final Object endpoint = new Object();
		EndpointInterceptor interceptor = new EndpointInterceptorAdapter();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertThat(givenRequest).isEqualTo(messageContext);
				return endpoint;
			}
		};
		mapping.setApplicationContext(applicationContext);
		mapping.setInterceptors(new EndpointInterceptor[] { interceptor });

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result.getInterceptors()).hasSize(2);
		assertThat(result.getInterceptors()[0]).isEqualTo(interceptor);
		assertThat(result.getInterceptors()[1]).isInstanceOf(MySmartEndpointInterceptor.class);
	}

	@Test
	public void endpointBeanName() throws Exception {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("endpoint", Object.class);

		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

			@Override
			protected Object getEndpointInternal(MessageContext message) throws Exception {
				assertThat(message).isEqualTo(messageContext);
				return "endpoint";
			}
		};
		mapping.setApplicationContext(applicationContext);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result).isNotNull();
	}

	@Test
	public void endpointInvalidBeanName() throws Exception {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("endpoint", Object.class);

		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

			@Override
			protected Object getEndpointInternal(MessageContext message) throws Exception {
				assertThat(message).isEqualTo(messageContext);
				return "noSuchBean";
			}
		};
		mapping.setApplicationContext(applicationContext);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result).isNull();
	}

	@Test
	public void endpointPrototype() throws Exception {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerPrototype("endpoint", MyEndpoint.class);

		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

			@Override
			protected Object getEndpointInternal(MessageContext message) throws Exception {
				assertThat(message).isEqualTo(messageContext);
				return "endpoint";
			}
		};
		mapping.setApplicationContext(applicationContext);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertThat(result).isNotNull();

		result = mapping.getEndpoint(messageContext);

		assertThat(result).isNotNull();
		assertThat(MyEndpoint.constructorCount).isEqualTo(2);
		MyEndpoint.reset();
	}


	private static class MyEndpoint {

		private static int constructorCount;

		private MyEndpoint() {
			constructorCount++;
		}

		private static void reset() {
			constructorCount = 0;
		}
	}

	private static class MySmartEndpointInterceptor extends DelegatingSmartEndpointInterceptor {

		private MySmartEndpointInterceptor() {
			super(new EndpointInterceptorAdapter());
		}
	}

}
