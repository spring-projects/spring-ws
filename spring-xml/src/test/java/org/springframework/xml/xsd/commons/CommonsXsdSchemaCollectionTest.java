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

package org.springframework.xml.xsd.commons;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.AbstractXsdSchemaTest;
import org.springframework.xml.xsd.XsdSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class CommonsXsdSchemaCollectionTest {

	private CommonsXsdSchemaCollection collection;

	private Transformer transformer;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	public void setUp() throws Exception {

		this.collection = new CommonsXsdSchemaCollection();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		this.transformer = transformerFactory.newTransformer();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	public void testSingle() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTest.class);
		this.collection.setXsds(resource);
		this.collection.afterPropertiesSet();

		assertThat(this.collection.getXsdSchemas()).hasSize(1);
	}

	@Test
	public void testInlineComplex() throws Exception {

		Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTest.class);
		this.collection.setXsds(a);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(2);
		assertThat(schemas[0].getTargetNamespace()).isEqualTo("urn:1");

		Resource abc = new ClassPathResource("ABC.xsd", AbstractXsdSchemaTest.class);
		Document expected = this.documentBuilder.parse(SaxUtils.createInputSource(abc));
		DOMResult domResult = new DOMResult();
		this.transformer.transform(schemas[0].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
		assertThat(schemas[1].getTargetNamespace()).isEqualTo("urn:2");

		Resource cd = new ClassPathResource("CD.xsd", AbstractXsdSchemaTest.class);
		expected = this.documentBuilder.parse(SaxUtils.createInputSource(cd));
		domResult = new DOMResult();
		this.transformer.transform(schemas[1].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testCircular() throws Exception {

		Resource resource = new ClassPathResource("circular-1.xsd", AbstractXsdSchemaTest.class);
		this.collection.setXsds(resource);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(1);
	}

	@Test

	public void testXmlNamespace() throws Exception {
		Resource resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTest.class);
		this.collection.setXsds(resource);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(1);
	}

	@Test
	public void testCreateValidator() throws Exception {

		Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTest.class);
		this.collection.setXsds(a);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();

		XmlValidator validator = this.collection.createValidator();

		assertThat(validator).isNotNull();
	}

	@Test
	public void testInvalidSchema() {

		Resource invalid = new ClassPathResource("invalid.xsd", AbstractXsdSchemaTest.class);
		this.collection.setXsds(invalid);

		assertThatExceptionOfType(CommonsXsdSchemaException.class)
			.isThrownBy(() -> this.collection.afterPropertiesSet());
	}

	@Test
	public void testIncludesAndImports() throws Exception {

		Resource hr = new ClassPathResource("hr.xsd", getClass());
		this.collection.setXsds(hr);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();

		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(2);
		assertThat(schemas[0].getTargetNamespace()).isEqualTo("http://mycompany.com/hr/schemas");

		Resource hr_employee = new ClassPathResource("hr_employee.xsd", getClass());
		Document expected = this.documentBuilder.parse(SaxUtils.createInputSource(hr_employee));
		DOMResult domResult = new DOMResult();
		this.transformer.transform(schemas[0].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
		assertThat(schemas[1].getTargetNamespace()).isEqualTo("http://mycompany.com/hr/schemas/holiday");

		Resource holiday = new ClassPathResource("holiday.xsd", getClass());
		expected = this.documentBuilder.parse(SaxUtils.createInputSource(holiday));
		domResult = new DOMResult();
		this.transformer.transform(schemas[1].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
	}

}
