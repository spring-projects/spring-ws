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

package org.springframework.xml.xsd;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.sax.SaxUtils;
import org.springframework.xml.validation.XmlValidator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.xmlunit.assertj.XmlAssert.assertThat;


public abstract class AbstractXsdSchemaTestCase {

	private DocumentBuilder documentBuilder;

	protected Transformer transformer;

	@Before
	public final void setUp() throws Exception {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformer = transformerFactory.newTransformer();
	}

	@Test
	public void testSingle() throws Exception {
		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema single = createSchema(resource);
		String namespace = "http://www.springframework.org/spring-ws/single/schema";
		Assert.assertEquals("Invalid target namespace", namespace, single.getTargetNamespace());
		resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(single.getSource(), domResult);
		Document result = (Document) domResult.getNode();
		assertThat(result).and(expected).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testIncludes() throws Exception {
		Resource resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema including = createSchema(resource);
		String namespace = "http://www.springframework.org/spring-ws/include/schema";
		Assert.assertEquals("Invalid target namespace", namespace, including.getTargetNamespace());
		resource = new ClassPathResource("including.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(including.getSource(), domResult);
		Document result = (Document) domResult.getNode();
		assertThat(result).and(expected).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testImports() throws Exception {
		Resource resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema importing = createSchema(resource);
		String namespace = "http://www.springframework.org/spring-ws/importing/schema";
		Assert.assertEquals("Invalid target namespace", namespace, importing.getTargetNamespace());
		resource = new ClassPathResource("importing.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(importing.getSource(), domResult);
		Document result = (Document) domResult.getNode();
		assertThat(result).and(expected).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testXmlNamespace() throws Exception {
		Resource resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema importing = createSchema(resource);
		String namespace = "http://www.springframework.org/spring-ws/xmlNamespace";
		Assert.assertEquals("Invalid target namespace", namespace, importing.getTargetNamespace());
		resource = new ClassPathResource("xmlNamespace.xsd", AbstractXsdSchemaTestCase.class);
		Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));
		DOMResult domResult = new DOMResult();
		transformer.transform(importing.getSource(), domResult);
		Document result = (Document) domResult.getNode();
		assertThat(result).and(expected).ignoreWhitespace().areSimilar();
	}

	@Test
	public void testCreateValidator() throws Exception {
		Resource resource = new ClassPathResource("single.xsd", AbstractXsdSchemaTestCase.class);
		XsdSchema single = createSchema(resource);
		XmlValidator validator = single.createValidator();
		Assert.assertNotNull("No XmlValidator returned", validator);
	}

	protected abstract XsdSchema createSchema(Resource resource) throws Exception;
}
