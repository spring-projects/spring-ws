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

package org.springframework.xml.validation;

import static org.assertj.core.api.Assertions.*;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SchemaLoaderUtilsTest {

	@Test
	public void testLoadSchema() throws Exception {

		Resource resource = new ClassPathResource("schema.xsd", getClass());
		Schema schema = SchemaLoaderUtils.loadSchema(resource, XMLConstants.W3C_XML_SCHEMA_NS_URI);

		assertThat(schema).isNotNull();
		assertThat(resource.isOpen()).isFalse();
	}

	@Test
	public void testLoadNonExistantSchema() throws Exception {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Resource nonExistent = new ClassPathResource("bla");
			SchemaLoaderUtils.loadSchema(nonExistent, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		});
	}

	@Test
	public void testLoadNullSchema() throws Exception {

		assertThatIllegalArgumentException()
				.isThrownBy(() -> SchemaLoaderUtils.loadSchema((Resource) null, XMLConstants.W3C_XML_SCHEMA_NS_URI));
	}

	@Test
	public void testLoadMultipleSchemas() throws Exception {

		Resource envelope = new ClassPathResource("envelope.xsd", getClass());
		Resource encoding = new ClassPathResource("encoding.xsd", getClass());
		Schema schema = SchemaLoaderUtils.loadSchema(new Resource[] { envelope, encoding },
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		assertThat(schema).isNotNull();
		assertThat(envelope.isOpen()).isFalse();
		assertThat(encoding.isOpen()).isFalse();
	}
}
