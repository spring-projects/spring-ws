/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ws.wsdl.wsdl11.provider;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import static org.assertj.core.api.Assertions.assertThat;

public class InliningXsdSchemaTypesProviderTests {

	private InliningXsdSchemaTypesProvider provider;

	private Definition definition;

	@BeforeEach
	public void setUp() throws Exception {

		this.provider = new InliningXsdSchemaTypesProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
	}

	@Test
	public void testSingle() throws Exception {

		String definitionNamespace = "http://springframework.org/spring-ws";
		this.definition.addNamespace("tns", definitionNamespace);
		this.definition.setTargetNamespace(definitionNamespace);
		String schemaNamespace = "http://www.springframework.org/spring-ws/schema";
		this.definition.addNamespace("schema", schemaNamespace);

		Resource resource = new ClassPathResource("schema.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();

		this.provider.setSchema(schema);

		this.provider.addTypes(this.definition);

		Types types = this.definition.getTypes();

		assertThat(types).isNotNull();
		assertThat(types.getExtensibilityElements()).hasSize(1);

		Schema wsdlSchema = (Schema) types.getExtensibilityElements().get(0);

		assertThat(wsdlSchema.getElement()).isNotNull();
	}

	@Test
	public void testComplex() throws Exception {

		String definitionNamespace = "http://springframework.org/spring-ws";
		this.definition.addNamespace("tns", definitionNamespace);
		this.definition.setTargetNamespace(definitionNamespace);
		String schemaNamespace = "http://www.springframework.org/spring-ws/schema";
		this.definition.addNamespace("schema", schemaNamespace);

		Resource resource = new ClassPathResource("A.xsd", getClass());
		CommonsXsdSchemaCollection collection = new CommonsXsdSchemaCollection(resource);
		collection.setInline(true);
		collection.afterPropertiesSet();

		this.provider.setSchemaCollection(collection);

		this.provider.addTypes(this.definition);

		Types types = this.definition.getTypes();

		assertThat(types).isNotNull();
		assertThat(types.getExtensibilityElements()).hasSize(2);

		Schema wsdlSchema = (Schema) types.getExtensibilityElements().get(0);

		assertThat(wsdlSchema.getElement()).isNotNull();

		wsdlSchema = (Schema) types.getExtensibilityElements().get(1);

		assertThat(wsdlSchema.getElement()).isNotNull();
	}

}
