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

package org.springframework.ws.server.endpoint.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

public class MessageEndpointAdapterTest {

	private MessageEndpointAdapter adapter;

	private MessageEndpoint endpointMock;

	@BeforeEach
	public void setUp() throws Exception {
		adapter = new MessageEndpointAdapter();
		endpointMock = createMock(MessageEndpoint.class);
	}

	@Test
	public void testSupports() {
		assertThat(adapter.supports(endpointMock)).isTrue();
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
