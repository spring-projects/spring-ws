/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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

class MessageEndpointAdapterTests {

	private MessageEndpointAdapter adapter;

	private MessageEndpoint endpointMock;

	@BeforeEach
	void setUp() {
		this.adapter = new MessageEndpointAdapter();
		this.endpointMock = createMock(MessageEndpoint.class);
	}

	@Test
	void testSupports() {
		assertThat(this.adapter.supports(this.endpointMock)).isTrue();
	}

	@Test
	void testInvoke() throws Exception {

		MessageContext context = new DefaultMessageContext(new MockWebServiceMessageFactory());

		this.endpointMock.invoke(context);

		replay(this.endpointMock);

		this.adapter.invoke(context, this.endpointMock);

		verify(this.endpointMock);
	}

}
