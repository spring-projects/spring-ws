/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import java.io.IOException;

import org.springframework.core.io.Resource;

class Jaxp15ValidatorFactoryTests extends AbstractValidatorFactoryTests {

	@Override
	protected XmlValidator createValidator(Resource[] schemaResources, String schemaLanguage) throws IOException {
		return Jaxp15ValidatorFactory.createValidator(schemaResources, schemaLanguage);
	}

}
