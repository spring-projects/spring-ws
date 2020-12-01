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

package org.springframework.xml.xsd;

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
import org.w3c.dom.Document;
import org.xmlunit.assertj.XmlAssert;

public abstract class AbstractXsdSchemaTestCase {

	private DocumentBuilder documentBuilder;

	protected Transformer transformer;

	@BeforeEach
	public final void setUp() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		TransformerFactory transformerFactory = TransformerFactoryUtils.newInstance();
		transformer = transformerFactory.newTransformer();
	}

	@Test
	public void testSingle() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema single = createSchema(resource);

		assertThat(single.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/single/schema");

		resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(single.getSource(), domResult);
		Document result = (Document) domResult.getNode();

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testIncludes() throws Exception {

		Resource resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema including = createSchema(resource);

		assertThat(including.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/include/schema");

		resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(including.getSource(), domResult);
		Document result = (Document) domResult.getNode();

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testImports() throws Exception {

		Resource resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema importing = createSchema(resource);

		assertThat(importing.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/importing/schema");

		resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(importing.getSource(), domResult);
		Document result = (Document) domResult.getNode();

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testXmlNamespace() throws Exception {

		Resource resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema importing = createSchema(resource);

		assertThat(importing.getTargetNamespace()).isEqualTo("http://www.springframework.org/spring-ws/xmlNamespace");

		resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(importing.getSource(), domResult);
		Document result = (Document) domResult.getNode();

		XmlAssert.assertThat(result).and(expected).ignoreWhitespace().areIdentical();
	}

	@Test
	public void testCreateValidator() throws Exception {

		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema single = createSchema(resource);
		XmlValidator validator = single.createValidator();

		assertThat(validator).isNotNull();
	}

	protected abstract XsdSchema createSchema(Resource resource) throws Exception;
}
