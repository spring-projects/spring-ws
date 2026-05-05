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

package org.springframework.ws.soap.addressing.server;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.server.test.OrderedEndpointInterceptor;
import org.springframework.ws.server.test.OrderedSmartEndpointInterceptor;
import org.springframework.ws.soap.addressing.server.test.TestAddressingEndpointMapping;
import org.springframework.ws.soap.saaj.test.SaajSoapMessages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AbstractAddressingEndpointMapping}.
 *
 * @author Stephane Nicoll
 */
class AbstractAddressingEndpointMappingTests {

	private final StaticApplicationContext applicationContext = new StaticApplicationContext();

	@Test
	void preInterceptorsAreInvokedAsConfigured() throws Exception {
		EndpointInterceptor firstInterceptor = OrderedEndpointInterceptor.mockInterceptor(-1);
		EndpointInterceptor secondInterceptor = OrderedEndpointInterceptor.mockInterceptor(1);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext,
				(beanConfiguration) -> beanConfiguration
					.setPreInterceptors(new EndpointInterceptor[] { secondInterceptor, firstInterceptor }));
		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly((first) -> assertThat(first).isSameAs(secondInterceptor),
				(second) -> assertThat(second).isSameAs(firstInterceptor),
				(third) -> assertThat(third).isInstanceOf(AddressingEndpointInterceptor.class));
	}

	@Test
	void smartInterceptorsAreInvokedInOrder() throws Exception {
		SmartEndpointInterceptor firstInterceptor = OrderedSmartEndpointInterceptor.mockInterceptor(-1, true);
		SmartEndpointInterceptor secondInterceptor = OrderedSmartEndpointInterceptor.mockInterceptor(1, true);
		this.applicationContext.getBeanFactory().registerSingleton("secondInterceptor", secondInterceptor);
		this.applicationContext.getBeanFactory().registerSingleton("firstInterceptor", firstInterceptor);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext);

		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly((first) -> assertThat(first).isSameAs(firstInterceptor),
				(second) -> assertThat(second).isInstanceOf(AddressingEndpointInterceptor.class),
				(third) -> assertThat(third).isSameAs(secondInterceptor));
	}

	@Test
	void smartInterceptorsAreInvokedOnlyIfTheyShouldIntercept() throws Exception {
		SmartEndpointInterceptor firstInterceptor = OrderedSmartEndpointInterceptor.mockInterceptor(-1, true);
		SmartEndpointInterceptor secondInterceptor = OrderedSmartEndpointInterceptor.mockInterceptor(1, false);
		this.applicationContext.getBeanFactory().registerSingleton("secondInterceptor", secondInterceptor);
		this.applicationContext.getBeanFactory().registerSingleton("firstInterceptor", firstInterceptor);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext);

		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly((first) -> assertThat(first).isSameAs(firstInterceptor),
				(second) -> assertThat(second).isInstanceOf(AddressingEndpointInterceptor.class));
	}

	@Test
	void regularInterceptorsAreInvokedInOrder() throws Exception {
		EndpointInterceptor firstInterceptor = OrderedEndpointInterceptor.mockInterceptor(-1);
		EndpointInterceptor secondInterceptor = OrderedEndpointInterceptor.mockInterceptor(1);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext,
				(beanConfiguration) -> beanConfiguration
					.setPostInterceptors(new EndpointInterceptor[] { secondInterceptor, firstInterceptor }));
		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly((first) -> assertThat(first).isSameAs(firstInterceptor),
				(second) -> assertThat(second).isInstanceOf(AddressingEndpointInterceptor.class),
				(third) -> assertThat(third).isSameAs(secondInterceptor));
	}

	@Test
	void smartInterceptorThatDoesNotImplementOrderedIsInvokedAfterAddressingInterceptor() throws Exception {
		SmartEndpointInterceptor endpointInterceptor = mock(SmartEndpointInterceptor.class);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext,
				(beanConfiguration) -> beanConfiguration
					.setPostInterceptors(new EndpointInterceptor[] { endpointInterceptor }));

		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly(
				(first) -> assertThat(first).isInstanceOf(AddressingEndpointInterceptor.class),
				(second) -> assertThat(second).isSameAs(endpointInterceptor));
	}

	@Test
	void regularInterceptorThatDoesNotImplementOrderedIsInvokedAfterAddressingInterceptor() throws Exception {
		EndpointInterceptor endpointInterceptor = mock(EndpointInterceptor.class);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext,
				(beanConfiguration) -> beanConfiguration
					.setPostInterceptors(new EndpointInterceptor[] { endpointInterceptor }));

		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly(
				(first) -> assertThat(first).isInstanceOf(AddressingEndpointInterceptor.class),
				(second) -> assertThat(second).isSameAs(endpointInterceptor));
	}

	@Test
	void combinedInterceptorsAreInvokedInOrder() throws Exception {
		SmartEndpointInterceptor smartInterceptor1 = OrderedSmartEndpointInterceptor.mockInterceptor(-1, true);
		SmartEndpointInterceptor smartInterceptor2 = OrderedSmartEndpointInterceptor.mockInterceptor(1, true);
		this.applicationContext.getBeanFactory().registerSingleton("smartInterceptor2", smartInterceptor2);
		this.applicationContext.getBeanFactory().registerSingleton("smartInterceptor1", smartInterceptor1);
		EndpointInterceptor preInterceptor = OrderedEndpointInterceptor.mockInterceptor(500);
		EndpointInterceptor postInterceptor = OrderedEndpointInterceptor.mockInterceptor(-2);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(this.applicationContext,
				(beanConfiguration) -> {
					beanConfiguration.setPostInterceptors(new EndpointInterceptor[] { postInterceptor });
					beanConfiguration.setPreInterceptors(new EndpointInterceptor[] { preInterceptor });
				});

		EndpointInvocationChain chain = mapping.getEndpoint(createMessageContext());
		assertThat(chain).isNotNull();
		assertThat(chain.getInterceptors()).satisfiesExactly((first) -> assertThat(first).isSameAs(preInterceptor),
				(second) -> assertThat(second).isSameAs(postInterceptor),
				(third) -> assertThat(third).isSameAs(smartInterceptor1),
				(fourth) -> assertThat(fourth).isInstanceOf(AddressingEndpointInterceptor.class),
				(fifth) -> assertThat(fifth).isSameAs(smartInterceptor2));
	}

	private MessageContext createMessageContext() {
		return SaajSoapMessages
			.createMessageContext(new ClassPathResource("org/springframework/ws/soap/addressing/200408/valid.xml"));
	}

}
