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

package org.springframework.ws.soap.security.wss4j2;

import java.util.function.Consumer;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.soap.addressing.server.AbstractAddressingEndpointMapping;
import org.springframework.ws.soap.addressing.server.test.TestAddressingEndpointMapping;
import org.springframework.ws.soap.saaj.test.SaajSoapMessages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Wss4jSecurityInterceptor}.
 *
 * @author Stephane Nicoll
 */
class Wss4jSecurityInterceptorTests {

	@Test
	public void wss4jSecurityInterceptorIsOrderedBeforeDefaultInterceptor() throws TransformerException {
		Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
		EndpointInterceptor interceptor = mock(EndpointInterceptor.class);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(new StaticApplicationContext(),
				(beanConfiguration) -> beanConfiguration
					.setPostInterceptors(new EndpointInterceptor[] { interceptor, securityInterceptor }));
		assertThat(mapping.getEndpoint(createMessageContext()))
			.satisfies((endpoint) -> assertThat(endpoint.getInterceptors()).satisfiesExactly(
					(first) -> assertThat(first).isSameAs(securityInterceptor), isAddressingInterceptor(),
					(third) -> assertThat(third).isSameAs(interceptor)));
	}

	@Test
	public void wss4jSecurityInterceptorIsOrderedBeforeDefaultSmartInterceptor() throws TransformerException {
		Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
		SmartEndpointInterceptor interceptor = mock(SmartEndpointInterceptor.class);
		when(interceptor.shouldIntercept(any(), any())).thenReturn(true);
		AbstractAddressingEndpointMapping mapping = TestAddressingEndpointMapping.create(new StaticApplicationContext(),
				(beanConfiguration) -> beanConfiguration
					.setPostInterceptors(new EndpointInterceptor[] { interceptor, securityInterceptor }));
		assertThat(mapping.getEndpoint(createMessageContext()))
			.satisfies((endpoint) -> assertThat(endpoint.getInterceptors()).satisfiesExactly(
					(first) -> assertThat(first).isSameAs(securityInterceptor), isAddressingInterceptor(),
					(third) -> assertThat(third).isSameAs(interceptor)));
	}

	private Consumer<EndpointInterceptor> isAddressingInterceptor() {
		return (endpointInterceptor) -> assertThat(endpointInterceptor.getClass().getSimpleName())
			.isEqualTo("AddressingEndpointInterceptor");
	}

	private MessageContext createMessageContext() {
		return SaajSoapMessages.createMessageContext(new ClassPathResource("echo-request.xml", getClass()));
	}

}
