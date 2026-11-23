/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xsd.commons;

import org.jspecify.annotations.Nullable;

import org.springframework.xml.xsd.XsdSchemaException;

/**
 * Commons XmlSchema version of the {@link XsdSchemaException}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class CommonsXsdSchemaException extends XsdSchemaException {

	public CommonsXsdSchemaException(String message) {
		super(message);
	}

	public CommonsXsdSchemaException(@Nullable String message, Throwable exception) {
		super(message, exception);
	}

}
