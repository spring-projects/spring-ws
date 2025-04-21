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

package org.springframework.xml.validation;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.xml.DocumentBuilderFactoryUtils;
import org.springframework.xml.transform.ResourceSource;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractValidatorFactoryTests {

	private XmlValidator validator;

	private InputStream validInputStream;

	private InputStream invalidInputStream;

	@BeforeEach
	public void setUp() throws Exception {

		Resource[] schemaResource = new Resource[] {
				new ClassPathResource("schema.xsd", AbstractValidatorFactoryTests.class) };
		this.validator = createValidator(schemaResource, XmlValidatorFactory.SCHEMA_W3C_XML);
		this.validInputStream = AbstractValidatorFactoryTests.class.getResourceAsStream("validDocument.xml");
		this.invalidInputStream = AbstractValidatorFactoryTests.class.getResourceAsStream("invalidDocument.xml");
	}

	@AfterEach
	public void tearDown() throws Exception {

		this.validInputStream.close();
		this.invalidInputStream.close();
	}

	protected abstract XmlValidator createValidator(Resource[] schemaResources, String schemaLanguage) throws Exception;

	@Test
	void testHandleValidMessageStream() throws Exception {

		SAXParseException[] errors = this.validator.validate(new StreamSource(this.validInputStream));

		assertThat(errors).isEmpty();
	}

	@Test
	void testValidateTwice() throws Exception {

		this.validator.validate(new StreamSource(this.validInputStream));
		this.validInputStream = AbstractValidatorFactoryTests.class.getResourceAsStream("validDocument.xml");
		this.validator.validate(new StreamSource(this.validInputStream));
	}

	@Test
	void testHandleInvalidMessageStream() throws Exception {

		SAXParseException[] errors = this.validator.validate(new StreamSource(this.invalidInputStream));

		assertThat(errors).hasSize(3);
	}

	@Test
	void testHandleValidMessageSax() throws Exception {

		SAXParseException[] errors = this.validator.validate(new SAXSource(new InputSource(this.validInputStream)));

		assertThat(errors).isEmpty();
	}

	@Test
	void testHandleInvalidMessageSax() throws Exception {

		SAXParseException[] errors = this.validator.validate(new SAXSource(new InputSource(this.invalidInputStream)));

		assertThat(errors).hasSize(3);
	}

	@Test
	void testHandleValidMessageDom() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(this.validInputStream));
		SAXParseException[] errors = this.validator.validate(new DOMSource(document));

		assertThat(errors).isEmpty();
	}

	@Test
	void testHandleInvalidMessageDom() throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactoryUtils.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		Document document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(this.invalidInputStream));
		SAXParseException[] errors = this.validator.validate(new DOMSource(document));

		assertThat(errors).hasSize(3);
	}

	@Test
	void testMultipleSchemasValidMessage() throws Exception {

		Resource[] schemaResources = new Resource[] {
				new ClassPathResource("multipleSchemas1.xsd", AbstractValidatorFactoryTests.class),
				new ClassPathResource("multipleSchemas2.xsd", AbstractValidatorFactoryTests.class) };
		this.validator = createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);

		Source document = new ResourceSource(
				new ClassPathResource("multipleSchemas1.xml", AbstractValidatorFactoryTests.class));
		SAXParseException[] errors = this.validator.validate(document);

		assertThat(errors).isEmpty();

		this.validator = createValidator(schemaResources, XmlValidatorFactory.SCHEMA_W3C_XML);
		document = new ResourceSource(
				new ClassPathResource("multipleSchemas2.xml", AbstractValidatorFactoryTests.class));
		errors = this.validator.validate(document);

		assertThat(errors).isEmpty();
	}

	@Test
	void customErrorHandler() throws Exception {

		ValidationErrorHandler myHandler = new ValidationErrorHandler() {
			public SAXParseException[] getErrors() {
				return new SAXParseException[0];
			}

			public void warning(SAXParseException exception) {
			}

			public void error(SAXParseException exception) {
			}

			public void fatalError(SAXParseException exception) {
			}
		};

		SAXParseException[] errors = this.validator.validate(new StreamSource(this.invalidInputStream), myHandler);

		assertThat(errors).isEmpty();
	}

}
