/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xml.validation;

import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.ResourceSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class AbstractValidatorFactoryTestCase {

	private XmlValidator validator;

	private InputStream validInputStream;

	private InputStream invalidInputStream;

	@BeforeEach
	public void setUp() throws Exception {

		Resource[] schemaResource = new Resource[] {
				new ClassPathResource("schema.xsd", AbstractValidatorFactoryTestCase.class) };
		validator = createValidator(schemaResource, XmlValidatorFactory.SCHEMA_W3C_XML);
		validInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("validDocument.xml");
		invalidInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("invalidDocument.xml");
	}

	@AfterEach
	public void tearDown() throws Exception {

		validInputStream.close();
		invalidInputStream.close();
	}

	protected abstract XmlValidator createValidator(Resource[] schemaResources, String schemaLanguage) throws Exception;

	@Test
	public void testHandleValidMessageStream() throws Exception {

		SAXParseException[] errors = validator.validate(new StreamSource(validInputStream));

		assertThat(errors).isEmpty();
	}

	@Test
	public void testValidateTwice() throws Exception {

		validator.validate(new StreamSource(validInputStream));
		validInputStream = AbstractValidatorFactoryTestCase.class.getResourceAsStream("validDocument.xml");
		validator.validate(new StreamSource(validInputStream));
	}

	@Test
	public void testHandleInvalidMessageStream() throws Exception {

		SAXParseException[] errors = validator.validate(new StreamSource(invalidInputStream));

		assertThat(errors).hasSize(3);
	}

	@Test
	public void testHandleValidMessageSax() throws Exception {

		SAXParseException[] errors = validator.validate(new SAXSource(new InputSource(validInputStream)));

		assertThat(errors).isEmpty();
	}

	@Test
	public void testHandleInvalidMessageSax() throws Exception {

		SAXParseException[] errors = validator.validate(new SAXSource(new InputSource(invalidInputStream)));

		assertThat(errors).hasSize(3);
	}

	@Test
	public void testHandleValidMessageDom() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(validInputStream));
		SAXParseException[] errors = validator.validate(new DOMSource(document));

		assertThat(errors).isEmpty();
		;
	}

	@Test
	public void testHandleInvalidMessageDom() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(invalidInputStream));
		SAXParseException[] errors = validator.validate(new DOMSource(document));

		assertThat(errors).hasSize(3);
	}

	@Test
	public void testMultipleSchemasValidMessage() throws Exception {

		Resource[] schemaResources = new Resource[] {
				new ClassPathResource("multipleSchemas1.xsd", AbstractValidatorFactoryTestCase.class),
				new ClassPathResource("multipleSchemas2.xsd", AbstractValidatorFactoryTestCase.class) };
		validator = createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);

		Source document = new ResourceSource(
				new ClassPathResource("multipleSchemas1.xml", AbstractValidatorFactoryTestCase.class));
		SAXParseException[] errors = validator.validate(document);

		assertThat(errors).isEmpty();

		validator = createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);
		document = new ResourceSource(
				new ClassPathResource("multipleSchemas2.xml", AbstractValidatorFactoryTestCase.class));
		errors = validator.validate(document);

		assertThat(errors).isEmpty();
		;
	}

	@Test
	public void customErrorHandler() throws Exception {

		ValidationErrorHandler myHandler = new ValidationErrorHandler() {
			public SAXParseException[] getErrors() {
				return new SAXParseException[0];
			}

			public void warning(SAXParseException exception) throws SAXException {}

			public void error(SAXParseException exception) throws SAXException {}

			public void fatalError(SAXParseException exception) throws SAXException {}
		};

		SAXParseException[] errors = validator.validate(new StreamSource(invalidInputStream), myHandler);

		assertThat(errors).isEmpty();
	}

}
