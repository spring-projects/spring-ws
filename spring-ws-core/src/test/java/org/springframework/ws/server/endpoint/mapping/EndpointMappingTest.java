/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.interceptor.DelegatingSmartEndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for {@link AbstractEndpointMapping}.
 */
public class EndpointMappingTest {

	private MessageContext messageContext;

	@Before
	public void setUp() throws Exception {
		messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@Test
	public void defaultEndpoint() throws Exception {
		Object defaultEndpoint = new Object();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertEquals("Invalid request passed", messageContext, givenRequest);
				return null;
			}
		};
		mapping.setDefaultEndpoint(defaultEndpoint);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);
		assertNotNull("No EndpointInvocatioChain returned", result);
		assertEquals("Default Endpoint not returned", defaultEndpoint, result.getEndpoint());
	}

	@Test
	public void endpoint() throws Exception {
		final Object endpoint = new Object();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertEquals("Invalid request passed", messageContext, givenRequest);
				return endpoint;
			}
		};

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);
		assertNotNull("No EndpointInvocationChain returned", result);
		assertEquals("Unexpected Endpoint returned", endpoint, result.getEndpoint());
	}

	@Test
	public void endpointInterceptors() throws Exception {
		final Object endpoint = new Object();
		EndpointInterceptor interceptor = new EndpointInterceptorAdapter();
		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {
			@Override
			protected Object getEndpointInternal(MessageContext givenRequest) throws Exception {
				assertEquals("Invalid request passed", messageContext, givenRequest);
				return endpoint;
			}
		};

		mapping.setInterceptors(new EndpointInterceptor[]{interceptor});
		EndpointInvocationChain result = mapping.getEndpoint(messageContext);
		assertEquals("Unexpected amount of EndpointInterceptors returned", 1, result.getInterceptors().length);
		assertEquals("Unexpected EndpointInterceptor returned", interceptor, result.getInterceptors()[0]);
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
				assertEquals("Invalid request passed", messageContext, givenRequest);
				return endpoint;
			}
		};
		mapping.setApplicationContext(applicationContext);
		mapping.setInterceptors(new EndpointInterceptor[]{interceptor});
		
		EndpointInvocationChain result = mapping.getEndpoint(messageContext);
		assertEquals("Unexpected amount of EndpointInterceptors returned", 2, result.getInterceptors().length);
		assertEquals("Unexpected EndpointInterceptor returned", interceptor, result.getInterceptors()[0]);
		assertTrue("Unexpected EndpointInterceptor returned",
				result.getInterceptors()[1] instanceof MySmartEndpointInterceptor);
	}

	@Test
	public void endpointBeanName() throws Exception {
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("endpoint", Object.class);

		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

			@Override
			protected Object getEndpointInternal(MessageContext message) throws Exception {
				assertEquals("Invalid request", messageContext, message);
				return "endpoint";
			}
		};
		mapping.setApplicationContext(applicationContext);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);
		assertNotNull("No endpoint returned", result);
	}

	@Test
	public void endpointInvalidBeanName() throws Exception {
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("endpoint", Object.class);

		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

			@Override
			protected Object getEndpointInternal(MessageContext message) throws Exception {
				assertEquals("Invalid request", messageContext, message);
				return "noSuchBean";
			}
		};
		mapping.setApplicationContext(applicationContext);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);

		assertNull("No endpoint returned", result);
	}

	@Test
	public void endpointPrototype() throws Exception {
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerPrototype("endpoint", MyEndpoint.class);

		AbstractEndpointMapping mapping = new AbstractEndpointMapping() {

			@Override
			protected Object getEndpointInternal(MessageContext message) throws Exception {
				assertEquals("Invalid request", messageContext, message);
				return "endpoint";
			}
		};
		mapping.setApplicationContext(applicationContext);

		EndpointInvocationChain result = mapping.getEndpoint(messageContext);
		assertNotNull("No endpoint returned", result);
		result = mapping.getEndpoint(messageContext);
		assertNotNull("No endpoint returned", result);
		assertEquals("Prototype endpoint was not constructed twice", 2, MyEndpoint.constructorCount);
	}

	private static class MyEndpoint {

		private static int constructorCount;

		private MyEndpoint() {
			constructorCount++;
		}
	}

	private static class MySmartEndpointInterceptor extends DelegatingSmartEndpointInterceptor {

		private MySmartEndpointInterceptor() {
			super(new EndpointInterceptorAdapter());
		}
	}

}
