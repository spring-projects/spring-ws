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

package org.springframework.ws.wsdl.wsdl11;

import static org.xmlunit.assertj.XmlAssert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;
import org.w3c.dom.Document;

public class DefaultWsdl11DefinitionTest {

	private DefaultWsdl11Definition definition;

	private Transformer transformer;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	public void setUp() throws Exception {

		definition = new DefaultWsdl11Definition();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	public void testSingle() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();
		definition.setSchema(schema);

		definition.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
		definition.setPortTypeName("Order");
		definition.setLocationUri("http://localhost:8080/");

		definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		transformer.transform(definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("single-inline.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testInclude() throws Exception {

		ClassPathResource resource = new ClassPathResource("including.xsd", getClass());
		CommonsXsdSchemaCollection schemaCollection = new CommonsXsdSchemaCollection(resource);
		schemaCollection.setInline(true);
		schemaCollection.afterPropertiesSet();
		definition.setSchemaCollection(schemaCollection);

		definition.setPortTypeName("Order");
		definition.setTargetNamespace("http://www.springframework.org/spring-ws/include/definitions");
		definition.setLocationUri("http://localhost:8080/");
		definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		transformer.transform(definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("include-inline.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testImport() throws Exception {

		ClassPathResource resource = new ClassPathResource("importing.xsd", getClass());
		CommonsXsdSchemaCollection schemaCollection = new CommonsXsdSchemaCollection(resource);
		schemaCollection.setInline(true);
		schemaCollection.afterPropertiesSet();
		definition.setSchemaCollection(schemaCollection);

		definition.setPortTypeName("Order");
		definition.setTargetNamespace("http://www.springframework.org/spring-ws/import/definitions");
		definition.setLocationUri("http://localhost:8080/");
		definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		transformer.transform(definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("import-inline.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testSoap11And12() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", getClass());
		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();
		definition.setSchema(schema);

		definition.setTargetNamespace("http://www.springframework.org/spring-ws/single/definitions");
		definition.setPortTypeName("Order");
		definition.setLocationUri("http://localhost:8080/");
		definition.setCreateSoap11Binding(true);
		definition.setCreateSoap12Binding(true);

		definition.afterPropertiesSet();

		DOMResult domResult = new DOMResult();
		transformer.transform(definition.getSource(), domResult);

		Document result = (Document) domResult.getNode();
		Document expected = documentBuilder.parse(getClass().getResourceAsStream("soap-11-12.wsdl"));

		assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}
}
