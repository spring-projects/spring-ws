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

package org.springframework.ws.server.endpoint.adapter.method;

import org.springframework.core.MethodParameter;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MessageContextMethodArgumentResolverTest {

	private MessageContextMethodArgumentResolver resolver;

	private MethodParameter supported;

	@Before
	public void setUp() throws NoSuchMethodException {
		resolver = new MessageContextMethodArgumentResolver();
		supported = new MethodParameter(getClass().getMethod("supported", MessageContext.class), 0);
	}

	@Test
	public void supportsParameter() throws Exception {
		assertTrue("resolver does not support MessageContext", resolver.supportsParameter(supported));
	}

	@Test
	public void resolveArgument() throws Exception {
		MessageContext messageContext = new DefaultMessageContext(new MockWebServiceMessageFactory());

		MessageContext result = resolver.resolveArgument(messageContext, supported);
		assertSame("Invalid message context returned", messageContext, result);
	}

	public void supported(MessageContext messageContext) {
	}

}
