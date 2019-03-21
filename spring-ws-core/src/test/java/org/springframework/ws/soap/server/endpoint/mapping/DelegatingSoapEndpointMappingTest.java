/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class DelegatingSoapEndpointMappingTest {

	private DelegatingSoapEndpointMapping endpointMapping;

	private EndpointMapping mock;

	@Before
	public void setUp() throws Exception {
		endpointMapping = new DelegatingSoapEndpointMapping();
		mock = createMock(EndpointMapping.class);
		endpointMapping.setDelegate(mock);
	}

	@Test
	public void testGetEndpointMapping() throws Exception {
		String role = "http://www.springframework.org/spring-ws/role";
		endpointMapping.setActorOrRole(role);
		MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());
		EndpointInvocationChain delegateChain = new EndpointInvocationChain(new Object());
		expect(mock.getEndpoint(context)).andReturn(delegateChain);

		replay(mock);

		SoapEndpointInvocationChain resultChain = (SoapEndpointInvocationChain) endpointMapping.getEndpoint(context);
		Assert.assertNotNull("No chain returned", resultChain);
		Assert.assertEquals("Invalid ampount of roles returned", 1, resultChain.getActorsOrRoles().length);
		Assert.assertEquals("Invalid role returned", role, resultChain.getActorsOrRoles()[0]);

		verify(mock);
	}
}