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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

public class MessageMethodEndpointAdapterTest {

	private MessageMethodEndpointAdapter adapter;

	private boolean supportedInvoked;

	private MessageContext messageContext;

	@BeforeEach
	public void setUp() throws Exception {
		adapter = new MessageMethodEndpointAdapter();
		messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@Test
	public void testSupported() throws NoSuchMethodException {

		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "supported", new Class[] { MessageContext.class });
		assertThat(adapter.supportsInternal(methodEndpoint)).isTrue();
	}

	@Test
	public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {

		assertThat(adapter.supportsInternal(new MethodEndpoint(this, "unsupportedMultipleParams",
				new Class[] { MessageContext.class, MessageContext.class }))).isFalse();
	}

	@Test
	public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {

		assertThat(
				adapter.supportsInternal(new MethodEndpoint(this, "unsupportedWrongParam", new Class[] { String.class })))
						.isFalse();
	}

	@Test
	public void testInvokeSupported() throws Exception {

		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "supported", new Class[] { MessageContext.class });

		assertThat(supportedInvoked).isFalse();

		adapter.invoke(messageContext, methodEndpoint);

		assertThat(supportedInvoked).isTrue();
	}

	public void supported(MessageContext context) {
		supportedInvoked = true;
	}

	public void unsupportedMultipleParams(MessageContext s1, MessageContext s2) {}

	public void unsupportedWrongParam(String request) {}
}
