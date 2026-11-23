/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.wsdl.wsdl11.provider;

import java.util.List;

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

class InliningXsdSchemaTypesProviderTests {

	private InliningXsdSchemaTypesProvider provider;

	private Definition definition;

	@BeforeEach
	void setUp() throws Exception {

		this.provider = new InliningXsdSchemaTypesProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
	}

	@Test
	void testSingle() throws Exception {

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
		assertThat(getExtensibilityElements(types)).hasSize(1);

		Schema wsdlSchema = (Schema) getExtensibilityElements(types).get(0);

		assertThat(wsdlSchema.getElement()).isNotNull();
	}

	@Test
	void testComplex() throws Exception {

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
		assertThat(getExtensibilityElements(types)).hasSize(2);

		Schema wsdlSchema = (Schema) getExtensibilityElements(types).get(0);

		assertThat(wsdlSchema.getElement()).isNotNull();

		wsdlSchema = (Schema) getExtensibilityElements(types).get(1);

		assertThat(wsdlSchema.getElement()).isNotNull();
	}

	private static List<?> getExtensibilityElements(Types types) {
		return types.getExtensibilityElements();
	}

}
