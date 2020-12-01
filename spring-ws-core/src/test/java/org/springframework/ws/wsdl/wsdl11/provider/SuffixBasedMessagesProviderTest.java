/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.w3c.dom.Document;

public class SuffixBasedMessagesProviderTest {

	private SuffixBasedMessagesProvider provider;

	private Definition definition;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	public void setUp() throws Exception {

		provider = new SuffixBasedMessagesProvider();
		provider.setFaultSuffix("Foo");
		WSDLFactory factory = WSDLFactory.newInstance();
		definition = factory.newDefinition();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	public void testAddMessages() throws Exception {

		String definitionNamespace = "http://springframework.org/spring-ws";
		definition.addNamespace("tns", definitionNamespace);
		definition.setTargetNamespace(definitionNamespace);
		String schemaNamespace = "http://www.springframework.org/spring-ws/schema";
		definition.addNamespace("schema", schemaNamespace);

		Resource resource = new ClassPathResource("schema.xsd", getClass());
		Document schemaDocument = documentBuilder.parse(SaxUtils.createInputSource(resource));
		Types types = definition.createTypes();
		definition.setTypes(types);
		Schema schema = (Schema) definition.getExtensionRegistry().createExtension(Types.class,
				new QName("http://www.w3.org/2001/XMLSchema", "schema"));
		types.addExtensibilityElement(schema);
		schema.setElement(schemaDocument.getDocumentElement());

		provider.addMessages(definition);

		assertThat(definition.getMessages()).hasSize(2);

		Message message = definition.getMessage(new QName(definitionNamespace, "GetOrderRequest"));

		assertThat(message).isNotNull();

		Part part = message.getPart("GetOrderRequest");

		assertThat(part).isNotNull();
		assertThat(part.getElementName()).isEqualTo(new QName(schemaNamespace, "GetOrderRequest"));

		message = definition.getMessage(new QName(definitionNamespace, "GetOrderResponse"));

		assertThat(message).isNotNull();

		part = message.getPart("GetOrderResponse");

		assertThat(part).isNotNull();
		assertThat(part.getElementName()).isEqualTo(new QName(schemaNamespace, "GetOrderResponse"));
	}
}
