/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xsd;

import javax.xml.transform.Source;

import org.jspecify.annotations.Nullable;

import org.springframework.xml.validation.XmlValidator;

/**
 * Represents an abstraction for XSD schemas.
 *
 * @author Mark LaFond
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public interface XsdSchema {

	/**
	 * Return the name of the schema, if known.
	 * @since 5.1.0
	 */
	default @Nullable String getName() {
		return null;
	}

	/**
	 * Returns the target namespace of this schema.
	 * @return the target namespace
	 */
	String getTargetNamespace();

	/**
	 * Returns the {@link Source} of the schema.
	 * @return the source of this XSD schema
	 */
	Source getSource();

	/**
	 * Creates a {@link XmlValidator} based on the schema.
	 * @return a validator for this schema
	 */
	XmlValidator createValidator();

}
