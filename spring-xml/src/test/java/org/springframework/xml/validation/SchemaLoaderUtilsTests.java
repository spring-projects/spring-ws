/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SchemaLoaderUtilsTests {

	@Test
	void testLoadSchema() throws Exception {

		Resource resource = new ClassPathResource("schema.xsd", getClass());
		Schema schema = SchemaLoaderUtils.loadSchema(resource, XMLConstants.W3C_XML_SCHEMA_NS_URI);

		assertThat(schema).isNotNull();
		assertThat(resource.isOpen()).isFalse();
	}

	@Test
	void testLoadNonExistantSchema() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Resource nonExistent = new ClassPathResource("bla");
			SchemaLoaderUtils.loadSchema(nonExistent, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		});
	}

	@Test
	void testLoadNullSchema() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> SchemaLoaderUtils.loadSchema((Resource) null, XMLConstants.W3C_XML_SCHEMA_NS_URI));
	}

	@Test
	void testLoadMultipleSchemas() throws Exception {

		Resource envelope = new ClassPathResource("envelope.xsd", getClass());
		Resource encoding = new ClassPathResource("encoding.xsd", getClass());
		Schema schema = SchemaLoaderUtils.loadSchema(new Resource[] { envelope, encoding },
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

		assertThat(schema).isNotNull();
		assertThat(envelope.isOpen()).isFalse();
		assertThat(encoding.isOpen()).isFalse();
	}

}
