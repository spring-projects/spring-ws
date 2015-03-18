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

package org.springframework.xml.validation;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.junit.Assert;
import org.junit.Test;

public class SchemaLoaderUtilsTest {

	@Test
	public void testLoadSchema() throws Exception {
		Resource resource = new ClassPathResource("schema.xsd", getClass());
		Schema schema = SchemaLoaderUtils.loadSchema(resource, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Assert.assertNotNull("No schema returned", schema);
		Assert.assertFalse("Resource not closed", resource.isOpen());
	}

	@Test
	public void testLoadNonExistantSchema() throws Exception {
		try {
			Resource nonExistant = new ClassPathResource("bla");
			SchemaLoaderUtils.loadSchema(nonExistant, XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Assert.fail("Should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testLoadNullSchema() throws Exception {
		try {
			SchemaLoaderUtils.loadSchema((Resource) null, XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Assert.fail("Should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testLoadMultipleSchemas() throws Exception {
		Resource envelope = new ClassPathResource("envelope.xsd", getClass());
		Resource encoding = new ClassPathResource("encoding.xsd", getClass());
		Schema schema =
				SchemaLoaderUtils.loadSchema(new Resource[]{envelope, encoding}, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Assert.assertNotNull("No schema returned", schema);
		Assert.assertFalse("Resource not closed", envelope.isOpen());
		Assert.assertFalse("Resource not closed", encoding.isOpen());
	}
}