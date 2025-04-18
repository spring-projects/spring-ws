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

package org.springframework.ws.wsdl.wsdl11;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import static org.xmlunit.assertj.XmlAssert.assertThat;

public class DefaultWsdl11DefinitionTests {

	private DefaultWsdl11Definition definition;

	private Transformer transformer;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	public void setUp() throws Exception {

		this.definition = new DefaultWsdl11Definition();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		this.transformer = transformerFactory.newTransformer();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	public void testSingle() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();
		this.definition.setSchema(schema);

		this.definition.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
		this.definition.setPortTypeName("Order");
		this.definition.setLocationUri("http://localhost:8080/");

		this.definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		this.transformer.transform(this.definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = this.documentBuilder.parse(getClass().getResourceAsStream("single-inline.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testInclude() throws Exception {

		ClassPathResource resource = new ClassPathResource("including.xsd", getClass());
		CommonsXsdSchemaCollection schemaCollection = new CommonsXsdSchemaCollection(resource);
		schemaCollection.setInline(true);
		schemaCollection.afterPropertiesSet();
		this.definition.setSchemaCollection(schemaCollection);

		this.definition.setPortTypeName("Order");
		this.definition.setTargetNamespace("http://www.springframework.org/spring-ws/include/definitions");
		this.definition.setLocationUri("http://localhost:8080/");
		this.definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		this.transformer.transform(this.definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = this.documentBuilder.parse(getClass().getResourceAsStream("include-inline.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testImport() throws Exception {

		ClassPathResource resource = new ClassPathResource("importing.xsd", getClass());
		CommonsXsdSchemaCollection schemaCollection = new CommonsXsdSchemaCollection(resource);
		schemaCollection.setInline(true);
		schemaCollection.afterPropertiesSet();
		this.definition.setSchemaCollection(schemaCollection);

		this.definition.setPortTypeName("Order");
		this.definition.setTargetNamespace("http://www.springframework.org/spring-ws/import/definitions");
		this.definition.setLocationUri("http://localhost:8080/");
		this.definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		this.transformer.transform(this.definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = this.documentBuilder.parse(getClass().getResourceAsStream("import-inline.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testSoap11And12() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();
		this.definition.setSchema(schema);

		this.definition.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
		this.definition.setPortTypeName("Order");
		this.definition.setLocationUri("http://localhost:8080/");
		this.definition.setCreateSoap11Binding(true);
		this.definition.setCreateSoap12Binding(true);

		this.definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		this.transformer.transform(this.definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = this.documentBuilder.parse(getClass().getResourceAsStream("soap-11-12.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

}
