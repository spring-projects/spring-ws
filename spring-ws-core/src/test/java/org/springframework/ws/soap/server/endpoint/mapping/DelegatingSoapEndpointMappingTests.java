/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
