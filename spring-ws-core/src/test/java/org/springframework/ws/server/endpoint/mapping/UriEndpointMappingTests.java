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

package org.springframework.ws.server.endpoint.mapping;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

class UriEndpointMappingTests {

	private UriEndpointMapping mapping;

	private MessageContext context;

	@BeforeEach
	void setUp() {

		this.mapping = new UriEndpointMapping();
		this.context = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@AfterEach
	void clearContext() {
		TransportContextHolder.setTransportContext(null);
	}

	@Test
	void getLookupKeyForMessage() throws Exception {

		WebServiceConnection connectionMock = createMock(WebServiceConnection.class);
		TransportContextHolder.setTransportContext(new DefaultTransportContext(connectionMock));

		URI uri = new URI("jms://exampleQueue");
		expect(connectionMock.getUri()).andReturn(uri);

		replay(connectionMock);

		assertThat(this.mapping.getLookupKeyForMessage(this.context)).isEqualTo(uri.toString());

		verify(connectionMock);
	}

	@Test
	void getLookupKeyForMessagePath() throws Exception {

		this.mapping.setUsePath(true);

		WebServiceConnection connectionMock = createMock(WebServiceConnection.class);
		TransportContextHolder.setTransportContext(new DefaultTransportContext(connectionMock));

		URI uri = new URI("http://example.com/foo/bar");
		expect(connectionMock.getUri()).andReturn(uri);

		replay(connectionMock);

		assertThat(this.mapping.getLookupKeyForMessage(this.context)).isEqualTo("/foo/bar");

		verify(connectionMock);
	}

	@Test
	void testValidateLookupKey() {

		assertThat(this.mapping.validateLookupKey("http://example.com/services")).isTrue();
		assertThat(this.mapping.validateLookupKey("some string")).isFalse();
	}

}
