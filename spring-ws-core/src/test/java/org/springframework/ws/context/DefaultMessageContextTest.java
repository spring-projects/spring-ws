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

package org.springframework.ws.context;

import java.util.Arrays;

import org.springframework.ws.MockWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class DefaultMessageContextTest {

	private DefaultMessageContext context;

	private WebServiceMessageFactory factoryMock;

	private WebServiceMessage request;

	@Before
	public void setUp() throws Exception {
		factoryMock = createMock(WebServiceMessageFactory.class);
		request = new MockWebServiceMessage();
		context = new DefaultMessageContext(request, factoryMock);
	}

	@Test
	public void testRequest() throws Exception {
		Assert.assertEquals("Invalid request returned", request, context.getRequest());
	}

	@Test
	public void testResponse() throws Exception {
		WebServiceMessage response = new MockWebServiceMessage();
		expect(factoryMock.createWebServiceMessage()).andReturn(response);
		replay(factoryMock);

		WebServiceMessage result = context.getResponse();
		Assert.assertEquals("Invalid response returned", response, result);
		verify(factoryMock);
	}

	@Test
	public void testProperties() throws Exception {
		Assert.assertEquals("Invalid property names returned", 0, context.getPropertyNames().length);
		String name = "name";
		Assert.assertFalse("Property set", context.containsProperty(name));
		String value = "value";
		context.setProperty(name, value);
		Assert.assertTrue("Property not set", context.containsProperty(name));
		Assert.assertEquals("Invalid property names returned", Arrays.asList(name),
				Arrays.asList(context.getPropertyNames()));
		Assert.assertEquals("Invalid property value returned", value, context.getProperty(name));
		context.removeProperty(name);
		Assert.assertFalse("Property set", context.containsProperty(name));
		Assert.assertEquals("Invalid property names returned", 0, context.getPropertyNames().length);
	}

}