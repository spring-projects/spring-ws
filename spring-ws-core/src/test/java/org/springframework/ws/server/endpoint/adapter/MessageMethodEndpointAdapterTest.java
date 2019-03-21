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

import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MessageMethodEndpointAdapterTest {

	private MessageMethodEndpointAdapter adapter;

	private boolean supportedInvoked;

	private MessageContext messageContext;

	@Before
	public void setUp() throws Exception {
		adapter = new MessageMethodEndpointAdapter();
		messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());
	}

	@Test
	public void testSupported() throws NoSuchMethodException {
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "supported", new Class[]{MessageContext.class});
		Assert.assertTrue("Method unsupported", adapter.supportsInternal(methodEndpoint));
	}

	@Test
	public void testUnsupportedMethodMultipleParams() throws NoSuchMethodException {
		Assert.assertFalse("Method supported", adapter.supportsInternal(
				new MethodEndpoint(this, "unsupportedMultipleParams",
						new Class[]{MessageContext.class, MessageContext.class})));
	}

	@Test
	public void testUnsupportedMethodWrongParam() throws NoSuchMethodException {
		Assert.assertFalse("Method supported",
				adapter.supportsInternal(new MethodEndpoint(this, "unsupportedWrongParam", new Class[]{String.class})));
	}

	@Test
	public void testInvokeSupported() throws Exception {
		MethodEndpoint methodEndpoint = new MethodEndpoint(this, "supported", new Class[]{MessageContext.class});
		Assert.assertFalse("Method invoked", supportedInvoked);
		adapter.invoke(messageContext, methodEndpoint);
		Assert.assertTrue("Method not invoked", supportedInvoked);
	}

	public void supported(MessageContext context) {
		supportedInvoked = true;
	}

	public void unsupportedMultipleParams(MessageContext s1, MessageContext s2) {
	}

	public void unsupportedWrongParam(String request) {
	}
}