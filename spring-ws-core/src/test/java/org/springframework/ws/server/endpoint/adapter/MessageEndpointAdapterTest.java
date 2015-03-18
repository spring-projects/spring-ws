/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.adapter;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class MessageEndpointAdapterTest {

	private MessageEndpointAdapter adapter;

	private MessageEndpoint endpointMock;

	@Before
	public void setUp() throws Exception {
		adapter = new MessageEndpointAdapter();
		endpointMock = createMock(MessageEndpoint.class);
	}

	@Test
	public void testSupports() throws Exception {
		Assert.assertTrue("MessageEndpointAdapter does not support MessageEndpoint", adapter.supports(endpointMock));
	}

	@Test
	public void testInvoke() throws Exception {
		MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());

		endpointMock.invoke(context);

		replay(endpointMock);

		adapter.invoke(context, endpointMock);

		verify(endpointMock);
	}



}