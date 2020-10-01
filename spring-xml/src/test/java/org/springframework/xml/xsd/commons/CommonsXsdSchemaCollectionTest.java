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

package org.springframework.xml.xsd.commons;

import static org.assertj.core.api.Assertions.*;

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
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.AbstractXsdSchemaTestCase;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

public class CommonsXsdSchemaCollectionTest {

	private CommonsXsdSchemaCollection collection;

	private Transformer transformer;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	public void setUp() throws Exception {

		collection = new CommonsXsdSchemaCollection();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	public void testSingle() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		collection.setXsds(resource);
		collection.afterPropertiesSet();

		assertThat(collection.getXsdSchemas()).hasSize(1);
	}

	@Test
	public void testInlineComplex() throws Exception {

		Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTestCase.class);
		collection.setXsds(a);
		collection.setInline(true);
		collection.afterPropertiesSet();
		XsdSchema[] schemas = collection.getXsdSchemas();

		assertThat(schemas).hasSize(2);
		assertThat(schemas[0].getTargetNamespace()).isEqualTo("urn:1");

		Resource abc = new ClassPathResource("ABC.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(abc));
		DOMResult domResult = new DOMResult();
		transformer.transform(schemas[0].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected) //
				.ignoreWhitespace() //
				.areIdentical();
		assertThat(schemas[1].getTargetNamespace()).isEqualTo("urn:2");

		Resource cd = new ClassPathResource("CD.xsd", AbstractXsdSchemaTestCase.class);
		expected = documentBuilder.parse(SaxUtils.createInputSource(cd));
		domResult = new DOMResult();
		transformer.transform(schemas[1].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected) //
				.ignoreWhitespace() //
				.areIdentical();
	}

	@Test
	public void testCircular() throws Exception {

		Resource resource = new ClassPathResource("circular-1.xsd", AbstractXsdSchemaTestCase.class);
		collection.setXsds(resource);
		collection.setInline(true);
		collection.afterPropertiesSet();
		XsdSchema[] schemas = collection.getXsdSchemas();

		assertThat(schemas).hasSize(1);
	}

	@Test

	public void testXmlNamespace() throws Exception {
		Resource resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTestCase.class);
		collection.setXsds(resource);
		collection.setInline(true);
		collection.afterPropertiesSet();
		XsdSchema[] schemas = collection.getXsdSchemas();

		assertThat(schemas).hasSize(1);
	}

	@Test
	public void testCreateValidator() throws Exception {

		Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTestCase.class);
		collection.setXsds(a);
		collection.setInline(true);
		collection.afterPropertiesSet();

		XmlValidator validator = collection.createValidator();

		assertThat(validator).isNotNull();
	}

	@Test
	public void testInvalidSchema() throws Exception {

		Resource invalid = new ClassPathResource("invalid.xsd", AbstractXsdSchemaTestCase.class);
		collection.setXsds(invalid);

		assertThatExceptionOfType(CommonsXsdSchemaException.class).isThrownBy(() -> collection.afterPropertiesSet());
	}

	@Test
	public void testIncludesAndImports() throws Exception {

		Resource hr = new ClassPathResource("hr.xsd", getClass());
		collection.setXsds(hr);
		collection.setInline(true);
		collection.afterPropertiesSet();

		XsdSchema[] schemas = collection.getXsdSchemas();

		assertThat(schemas).hasSize(2);
		assertThat(schemas[0].getTargetNamespace()).isEqualTo("http://mycompany.com/hr/schemas");

		Resource hr_employee = new ClassPathResource("hr_employee.xsd", getClass());
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(hr_employee));
		DOMResult domResult = new DOMResult();
		transformer.transform(schemas[0].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
		assertThat(schemas[1].getTargetNamespace()).isEqualTo("http://mycompany.com/hr/schemas/holiday");

		Resource holiday = new ClassPathResource("holiday.xsd", getClass());
		expected = documentBuilder.parse(SaxUtils.createInputSource(holiday));
		domResult = new DOMResult();
		transformer.transform(schemas[1].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
	}
}
