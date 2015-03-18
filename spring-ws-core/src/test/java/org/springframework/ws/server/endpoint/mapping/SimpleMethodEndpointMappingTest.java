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

package org.springframework.ws.server.endpoint.mapping;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.MockWebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleMethodEndpointMappingTest {

	private SimpleMethodEndpointMapping mapping;

	@Before
	public void setUp() throws Exception {
		mapping = new SimpleMethodEndpointMapping();
		mapping.setMethodPrefix("prefix");
		mapping.setMethodSuffix("Suffix");
		MyBean bean = new MyBean();
		mapping.setEndpoints(new Object[]{bean});
		mapping.afterPropertiesSet();
	}

	@Test
	public void testRegistration() throws Exception {
		Assert.assertNotNull("Endpoint not registered", mapping.lookupEndpoint("MyRequest"));
		Assert.assertNull("Endpoint registered", mapping.lookupEndpoint("request"));
	}

	@Test
	public void testGetLookupKeyForMessageNoNamespace() throws Exception {
		MockWebServiceMessage request = new MockWebServiceMessage("<MyRequest/>");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		String result = mapping.getLookupKeyForMessage(messageContext);
		Assert.assertEquals("Invalid lookup key", "MyRequest", result);
	}

	@Test
	public void testGetLookupKeyForMessageNamespace() throws Exception {
		MockWebServiceMessage request =
				new MockWebServiceMessage("<MyRequest xmlns='http://springframework.org/spring-ws/' />");
		MessageContext messageContext = new DefaultMessageContext(request, new MockWebServiceMessageFactory());
		String result = mapping.getLookupKeyForMessage(messageContext);
		Assert.assertEquals("Invalid lookup key", "MyRequest", result);
	}

	private static class MyBean {

		public void prefixMyRequestSuffix() {

		}

		public void request() {

		}
	}
}