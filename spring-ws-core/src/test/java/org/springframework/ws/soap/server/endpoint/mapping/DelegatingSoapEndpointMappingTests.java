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

package org.springframework.ws.soap.server.endpoint.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class DelegatingSoapEndpointMappingTests {

	private DelegatingSoapEndpointMapping endpointMapping;

	private EndpointMapping mock;

	@BeforeEach
	void setUp() {

		this.endpointMapping = new DelegatingSoapEndpointMapping();
		this.mock = createMock(EndpointMapping.class);
		this.endpointMapping.setDelegate(this.mock);
	}

	@Test
	void testGetEndpointMapping() throws Exception {

		String role = "http://www.springframework.org/spring-ws/role";
		this.endpointMapping.setActorOrRole(role);
		MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());
		EndpointInvocationChain delegateChain = new EndpointInvocationChain(new Object());
		expect(this.mock.getEndpoint(context)).andReturn(delegateChain);

		replay(this.mock);

		SoapEndpointInvocationChain resultChain = (SoapEndpointInvocationChain) this.endpointMapping
			.getEndpoint(context);

		assertThat(resultChain).isNotNull();
		assertThat(resultChain.getActorsOrRoles()).containsExactly(role);

		verify(this.mock);
	}

}
