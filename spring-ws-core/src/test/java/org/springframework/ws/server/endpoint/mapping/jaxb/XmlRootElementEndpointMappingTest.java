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

package org.springframework.ws.server.endpoint.mapping.jaxb;

import java.lang.reflect.Method;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.springframework.web.bind.annotation.RequestBody;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class XmlRootElementEndpointMappingTest {

	private XmlRootElementEndpointMapping mapping;

	@Before
	public void createMapping() throws NoSuchMethodException {
		mapping = new XmlRootElementEndpointMapping();
	}

	@Test
	public void rootElement() throws NoSuchMethodException {
		Method rootElement = getClass().getMethod("rootElement", MyRootElement.class);
		QName name = mapping.getLookupKeyForMethod(rootElement);
		assertEquals(new QName("myNamespace", "myRoot"), name);
	}

	@Test
	public void testRequiredRequestBodyAnnotationMissing() throws NoSuchMethodException {
		mapping.setRequiresRequestBodyAnnotation(true);

		Method rootElement = getClass().getMethod("rootElement", MyRootElement.class);
		QName name = mapping.getLookupKeyForMethod(rootElement);
		assertNull(name);
	}

	@Test
	public void testRequiredRequestBodyAnnotationPresent() throws NoSuchMethodException {
		mapping.setRequiresRequestBodyAnnotation(true);

		Method rootElement = getClass().getMethod("requestBodyRootElement", MyRootElement.class);
		QName name = mapping.getLookupKeyForMethod(rootElement);
		assertEquals(new QName("myNamespace", "myRoot"), name);
	}

	public void rootElement(MyRootElement rootElement) {
	}

	public void requestBodyRootElement(@RequestBody MyRootElement rootElement) {
	}

	@XmlRootElement(name = "myRoot", namespace = "myNamespace")
	public static class MyRootElement {

	}

}
