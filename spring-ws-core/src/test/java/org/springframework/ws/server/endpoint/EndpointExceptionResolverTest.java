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

package org.springframework.ws.server.endpoint;

import java.util.Collections;

import org.springframework.ws.context.MessageContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for AbstractEndpointExceptionResolver
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 */
public class EndpointExceptionResolverTest {

	private MethodEndpoint methodEndpoint;

	private AbstractEndpointExceptionResolver exceptionResolver;

	@Before
	public void setUp() throws Exception {
		exceptionResolver = new AbstractEndpointExceptionResolver() {

			@Override
			protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
				return true;
			}
		};

		exceptionResolver.setMappedEndpoints(Collections.singleton(this));
		methodEndpoint = new MethodEndpoint(this, getClass().getMethod("emptyMethod", new Class[0]));
	}

	@Test
	public void testMatchMethodEndpoint() {
		boolean matched = exceptionResolver.resolveException(null, methodEndpoint, null);
		Assert.assertTrue("AbstractEndpointExceptionResolver did not match mapped MethodEndpoint", matched);
	}

	public void emptyMethod() {
	}
}
