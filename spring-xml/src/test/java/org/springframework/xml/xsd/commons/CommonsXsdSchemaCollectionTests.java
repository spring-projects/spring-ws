/*
 * Copyright 2005-present the original author or authors.
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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.transform.TransformerFactoryUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.AbstractXsdSchemaTests;
import org.springframework.xml.xsd.XsdSchema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CommonsXsdSchemaCollectionTests {

	private CommonsXsdSchemaCollection collection;

	private Transformer transformer;

	private DocumentBuilder documentBuilder;

	@BeforeEach
	void setUp() throws Exception {

		this.collection = new CommonsXsdSchemaCollection();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		this.transformer = transformerFactory.newTransformer();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	void testSingle() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTests.class);
		this.collection.setXsds(resource);
		this.collection.afterPropertiesSet();

		assertThat(this.collection.getXsdSchemas()).hasSize(1);
	}

	@Test
	void testInlineComplex() throws Exception {

		Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTests.class);
		this.collection.setXsds(a);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(2);
		assertThat(schemas[0].getTargetNamespace()).isEqualTo("urn:1");

		Resource abc = new ClassPathResource("ABC.xsd", AbstractXsdSchemaTests.class);
		Document expected = this.documentBuilder.parse(SaxUtils.createInputSource(abc));
		DOMResult domResult = new DOMResult();
		this.transformer.transform(schemas[0].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
		assertThat(schemas[1].getTargetNamespace()).isEqualTo("urn:2");

		Resource cd = new ClassPathResource("CD.xsd", AbstractXsdSchemaTests.class);
		expected = this.documentBuilder.parse(SaxUtils.createInputSource(cd));
		domResult = new DOMResult();
		this.transformer.transform(schemas[1].getSource(), domResult);

		XmlAssert.assertThat(domResult.getNode()).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	void testCircular() throws Exception {

		Resource resource = new ClassPathResource("circular-1.xsd", AbstractXsdSchemaTests.class);
		this.collection.setXsds(resource);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(1);
	}

	@Test
	void testXmlNamespace() throws Exception {
		Resource resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTests.class);
		this.collection.setXsds(resource);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();

		assertThat(schemas).hasSize(1);
	}

	@Test
	void testCreateValidator() throws Exception {

		Resource a = new ClassPathResource("A.xsd", AbstractXsdSchemaTests.class);
		this.collection.setXsds(a);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();

		XmlValidator validator = this.collection.createValidator();

		assertThat(validator).isNotNull();
	}

	@Test
	void testInvalidSchema() {

		Resource invalid = new ClassPathResource("invalid.xsd", AbstractXsdSchemaTests.class);
		this.collection.setXsds(invalid);

		assertThatExceptionOfType(CommonsXsdSchemaException.class)
			.isThrownBy(() -> this.collection.afterPropertiesSet());
	}

	@Test
	void resolveEntityFallsBackToBaseUriWhenResourceLoaderRejectsRelativeLocation() throws Exception {
		Resource order = new ClassPathResource("relative/order.xsd", getClass());
		this.collection.setResourceLoader(new PathTraversalRejectingResourceLoader());
		this.collection.setXsds(order);
		this.collection.setInline(true);
		this.collection.afterPropertiesSet();
		XsdSchema[] schemas = this.collection.getXsdSchemas();
		assertThat(schemas).hasSize(2);
		assertThat(schemas[0].getTargetNamespace()).isEqualTo("http://mycompany.com/relative/order");
		assertThat(schemas[1].getTargetNamespace()).isEqualTo("http://mycompany.com/relative/common");
	}

	@Test
	void testIncludesAndImports() throws Exception {

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

	/**
	 * Mimics a {@code ResourceLoader} backed by a Servlet container (such as Tomcat) that
	 * rejects locations attempting to traverse outside a given root, instead of simply
	 * reporting the resource as non-existent.
	 */
	private static final class PathTraversalRejectingResourceLoader implements ResourceLoader {

		private final ResourceLoader delegate = new DefaultResourceLoader();

		@Override
		public Resource getResource(String location) {
			if (location.contains("..")) {
				throw new IllegalArgumentException(
						"The resource path [" + location + "] has been normalized to [null] which is not valid");
			}
			return this.delegate.getResource(location);
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.delegate.getClassLoader();
		}

	}

}
