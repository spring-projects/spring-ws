/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.xml.validation;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class XmlValidatorFactoryTest {

	@Test
	public void testCreateValidator() throws Exception {

		Resource resource = new ClassPathResource("schema.xsd", AbstractValidatorFactoryTestCase.class);
		XmlValidator validator = XmlValidatorFactory.createValidator(resource, XmlValidatorFactory.SCHEMA_W3C_XML);

		assertThat(validator).isNotNull();
	}

	@Test
	public void testNonExistentResource() {

		assertThatIllegalArgumentException().isThrownBy(
				() -> XmlValidatorFactory.createValidator(new NonExistentResource(), XmlValidatorFactory.SCHEMA_W3C_XML));
	}

	@Test
	public void testInvalidSchemaLanguage() {

		assertThatIllegalArgumentException().isThrownBy(() -> XmlValidatorFactory
				.createValidator(new ClassPathResource("schema.xsd", AbstractValidatorFactoryTestCase.class), "bla"));
	}

	private static class NonExistentResource extends AbstractResource {

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
