/*
 * Copyright 2005-2025 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class MessageEndpointAdapterTests {

	private MessageEndpointAdapter adapter;

	private MessageEndpoint endpointMock;

	@BeforeEach
	public void setUp() {
		this.adapter = new MessageEndpointAdapter();
		this.endpointMock = createMock(MessageEndpoint.class);
	}

	@Test
	public void testSupports() {
		assertThat(this.adapter.supports(this.endpointMock)).isTrue();
	}

	@Test
	public void testInvoke() throws Exception {

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());

		this.endpointMock.invoke(context);

		replay(this.endpointMock);

		this.adapter.invoke(context, this.endpointMock);

		verify(this.endpointMock);
	}

}
