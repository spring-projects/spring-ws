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

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

public class InliningXsdSchemaTypesProviderTest {

	private InliningXsdSchemaTypesProvider provider;

	private Definition definition;

	@Before
	public void setUp() throws Exception {
		provider = new InliningXsdSchemaTypesProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		definition = factory.newDefinition();
	}

	@Test
	public void testSingle() throws Exception {
		String definitionNamespace = "http://springframework.org/spring-ws";
		definition.addNamespace("tns", definitionNamespace);
		definition.setTargetNamespace(definitionNamespace);
		String schemaNamespace = "http://www.springframework.org/spring-ws/schema";
		definition.addNamespace("schema", schemaNamespace);

		Resource resource = new ClassPathResource("schema.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();

		provider.setSchema(schema);

		provider.addTypes(definition);

		Types types = definition.getTypes();
		Assert.assertNotNull("No types created", types);
		Assert.assertEquals("Invalid amount of schemas", 1, types.getExtensibilityElements().size());

		Schema wsdlSchema = (Schema) types.getExtensibilityElements().get(0);
		Assert.assertNotNull("No element defined", wsdlSchema.getElement());
	}

	@Test
	public void testComplex() throws Exception {
		String definitionNamespace = "http://springframework.org/spring-ws";
		definition.addNamespace("tns", definitionNamespace);
		definition.setTargetNamespace(definitionNamespace);
		String schemaNamespace = "http://www.springframework.org/spring-ws/schema";
		definition.addNamespace("schema", schemaNamespace);

		Resource resource = new ClassPathResource("A.xsd", getClass());
		CommonsXsdSchemaCollection collection = new CommonsXsdSchemaCollection(resource);
		collection.setInline(true);
		collection.afterPropertiesSet();

		provider.setSchemaCollection(collection);

		provider.addTypes(definition);

		Types types = definition.getTypes();
		Assert.assertNotNull("No types created", types);
		Assert.assertEquals("Invalid amount of schemas", 2, types.getExtensibilityElements().size());

		Schema wsdlSchema = (Schema) types.getExtensibilityElements().get(0);
		Assert.assertNotNull("No element defined", wsdlSchema.getElement());

		wsdlSchema = (Schema) types.getExtensibilityElements().get(1);
		Assert.assertNotNull("No element defined", wsdlSchema.getElement());
	}
}
