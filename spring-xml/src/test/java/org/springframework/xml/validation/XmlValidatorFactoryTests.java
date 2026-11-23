/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class XmlValidatorFactoryTests {

	@Test
	void testCreateValidator() throws Exception {

		Resource resource = new ClassPathResource("schema.xsd", AbstractValidatorFactoryTests.class);
		XmlValidator validator = XmlValidatorFactory.createValidator(resource, XmlValidatorFactory.SCHEMA_W3C_XML);

		assertThat(validator).isNotNull();
	}

	@Test
	void testNonExistentResource() {

		assertThatIllegalArgumentException().isThrownBy(() -> XmlValidatorFactory
			.createValidator(new NonExistentResource(), XmlValidatorFactory.SCHEMA_W3C_XML));
	}

	@Test
	void testInvalidSchemaLanguage() {

		assertThatIllegalArgumentException().isThrownBy(() -> XmlValidatorFactory
			.createValidator(new ClassPathResource("schema.xsd", AbstractValidatorFactoryTests.class), "bla"));
	}

	private static final class NonExistentResource extends AbstractResource {

		@Override
		public Resource createRelative(String relativePath) throws IOException {
			throw new IOException();
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public File getFile() throws IOException {
			throw new IOException();
		}

		@Override
		public String getFilename() {
			return null;
		}

		@Override
		public URL getURL() throws IOException {
			throw new IOException();
		}

		@Override
		public URI getURI() throws IOException {
			throw new IOException();
		}

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			throw new IOException();
		}

		@Override
		public boolean isReadable() {
			return false;
		}

	}

}
