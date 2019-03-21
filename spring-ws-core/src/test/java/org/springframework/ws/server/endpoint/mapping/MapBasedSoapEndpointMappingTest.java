/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.context.MessageContext;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for AbstractMapBasedEndpointMapping.
 */
public class MapBasedSoapEndpointMappingTest {

	@Test
	public void testBeanNames() throws Exception {
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerSingleton("endpointMapping", MyMapBasedEndpointMapping.class);
		context.registerSingleton("endpoint", Object.class);
		context.registerAlias("endpoint", "alias");
		MyMapBasedEndpointMapping mapping = new MyMapBasedEndpointMapping();
		mapping.setValidKeys(new String[]{"endpoint", "alias"});

		mapping.setRegisterBeanNames(true);
		mapping.setApplicationContext(context);

		// try bean
		mapping.setKey("endpoint");
		Assert.assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

		// try alias
		mapping.setKey("alias");
		Assert.assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

		// try non-mapped values
		mapping.setKey("endpointMapping");
		Assert.assertNull("Endpoint returned", mapping.getEndpointInternal(null));

	}

	@Test
	public void testDisabledBeanNames() throws Exception {
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerSingleton("endpoint", Object.class);

		MyMapBasedEndpointMapping mapping = new MyMapBasedEndpointMapping();

		mapping.setRegisterBeanNames(true);
		mapping.setApplicationContext(context);

		mapping.setKey("endpoint");
		Assert.assertNull("Endpoint returned", mapping.getEndpointInternal(null));
	}

	@Test
	public void testEndpointMap() throws Exception {
		Map<String, Object> endpointMap = new TreeMap<String, Object>();
		Object endpoint1 = new Object();
		Object endpoint2 = new Object();
		endpointMap.put("endpoint1", endpoint1);
		endpointMap.put("endpoint2", endpoint2);

		MyMapBasedEndpointMapping mapping = new MyMapBasedEndpointMapping();
		mapping.setValidKeys(new String[]{"endpoint1", "endpoint2"});

		mapping.setEndpointMap(endpointMap);
		mapping.setApplicationContext(new StaticApplicationContext());

		// try endpoint1
		mapping.setKey("endpoint1");
		Assert.assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

		// try endpoint2
		mapping.setKey("endpoint2");
		Assert.assertNotNull("No endpoint returned", mapping.getEndpointInternal(null));

		// try non-mapped values
		mapping.setKey("endpoint3");
		Assert.assertNull("Endpoint returned", mapping.getEndpointInternal(null));
	}

	private static class MyMapBasedEndpointMapping extends AbstractMapBasedEndpointMapping {

		private String key;

		private String[] validKeys = new String[0];

		public void setKey(String key) {
			this.key = key;
		}

		public void setValidKeys(String[] validKeys) {
			this.validKeys = validKeys;
			Arrays.sort(this.validKeys);
		}

		@Override
		protected boolean validateLookupKey(String key) {
			return Arrays.binarySearch(validKeys, key) >= 0;
		}

		@Override
		protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
			return key;
		}
	}

}