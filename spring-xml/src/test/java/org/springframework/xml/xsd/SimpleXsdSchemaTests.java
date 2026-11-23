/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xsd;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class SimpleXsdSchemaTests extends AbstractXsdSchemaTests {

	@Override
	protected XsdSchema createSchema(Resource resource) throws Exception {

		SimpleXsdSchema schema = new SimpleXsdSchema(resource);
		schema.afterPropertiesSet();

		return schema;
	}

	@Test
	void testBareXsdSchema() {
		assertThatIllegalStateException().isThrownBy(() -> new SimpleXsdSchema().toString());
	}

}
