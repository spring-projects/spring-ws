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
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMessagesProviderTests {

	private DefaultMessagesProvider provider;

	private Definition definition;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	void setUp() throws Exception {

		this.provider = new DefaultMessagesProvider();
		WSDLFactory factory = WSDLFactory.newInstance();
		this.definition = factory.newDefinition();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	@SuppressWarnings("unchecked")
	void testAddMessages() throws Exception {

		String definitionNamespace = "http://springframework.org/spring-ws";
		this.definition.addNamespace("tns", definitionNamespace);
		this.definition.setTargetNamespace(definitionNamespace);
		String schemaNamespace = "http://www.springframework.org/spring-ws/schema";
		this.definition.addNamespace("schema", schemaNamespace);

		Resource resource = new ClassPathResource("schema.xsd", getClass());
		Document schemaDocument = this.documentBuilder.parse(SaxUtils.createInputSource(resource));
		Types types = this.definition.createTypes();
		this.definition.setTypes(types);
		Schema schema = (Schema) this.definition.getExtensionRegistry()
			.createExtension(Types.class, new QName("http://www.w3.org/2001/XMLSchema", "schema"));
		types.addExtensibilityElement(schema);
		schema.setElement(schemaDocument.getDocumentElement());

		this.provider.addMessages(this.definition);

		assertThat(this.definition.getMessages()).hasSize(3);

		Message message = this.definition.getMessage(new QName(definitionNamespace, "GetOrderRequest"));

		assertThat(message).isNotNull();

		Part part = message.getPart("GetOrderRequest");

		assertThat(part).isNotNull();
		assertThat(part.getElementName()).isEqualTo(new QName(schemaNamespace, "GetOrderRequest"));

		message = this.definition.getMessage(new QName(definitionNamespace, "GetOrderResponse"));

		assertThat(message).isNotNull();

		part = message.getPart("GetOrderResponse");

		assertThat(part).isNotNull();
		assertThat(part.getElementName()).isEqualTo(new QName(schemaNamespace, "GetOrderResponse"));

		message = this.definition.getMessage(new QName(definitionNamespace, "GetOrderFault"));

		assertThat(message).isNotNull();

		part = message.getPart("GetOrderFault");

		assertThat(part).isNotNull();
		assertThat(part.getElementName()).isEqualTo(new QName(schemaNamespace, "GetOrderFault"));
	}

}
