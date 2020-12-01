/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import static org.assertj.core.api.Assertions.*;
import static org.easymock.EasyMock.*;

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

public class UriEndpointMappingTest {

	private UriEndpointMapping mapping;

	private MessageContext context;

	@BeforeEach
	public void setUp() throws Exception {

		mapping = new UriEndpointMapping();
		context = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@AfterEach
	public void clearContext() {
		TransportContextHolder.setTransportContext(null);
	}

	@Test
	public void getLookupKeyForMessage() throws Exception {

		WebServiceConnection connectionMock = createMock(WebServiceConnection.class);
		TransportContextHolder.setTransportContext(new DefaultTransportContext(connectionMock));

		URI uri = new URI("jms://exampleQueue");
		expect(connectionMock.getUri()).andReturn(uri);

		replay(connectionMock);

		assertThat(mapping.getLookupKeyForMessage(context)).isEqualTo(uri.toString());

		verify(connectionMock);
	}

	@Test
	public void getLookupKeyForMessagePath() throws Exception {

		mapping.setUsePath(true);

		WebServiceConnection connectionMock = createMock(WebServiceConnection.class);
		TransportContextHolder.setTransportContext(new DefaultTransportContext(connectionMock));

		URI uri = new URI("http://example.com/foo/bar");
		expect(connectionMock.getUri()).andReturn(uri);

		replay(connectionMock);

		assertThat(mapping.getLookupKeyForMessage(context)).isEqualTo("/foo/bar");

		verify(connectionMock);
	}

	@Test
	public void testValidateLookupKey() {

		assertThat(mapping.validateLookupKey("http://example.com/services")).isTrue();
		assertThat(mapping.validateLookupKey("some string")).isFalse();
	}
}
