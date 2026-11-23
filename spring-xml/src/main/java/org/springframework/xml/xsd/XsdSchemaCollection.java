/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xsd;

import org.springframework.xml.validation.XmlValidator;

/**
 * Represents an abstraction for a collection of XSD schemas.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface XsdSchemaCollection {

	/**
	 * Returns all schemas contained in this collection.
	 * @return the schemas contained in this collection
	 */
	XsdSchema[] getXsdSchemas();

	/**
	 * Creates a {@link XmlValidator} based on the schemas contained in this collection.
	 * @return a validator for this collection
	 */
	XmlValidator createValidator();

}
