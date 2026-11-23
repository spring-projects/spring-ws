/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xsd;

import org.jspecify.annotations.Nullable;

import org.springframework.xml.XmlException;

/**
 * Base class for all WSDL definition exceptions.
 *
 * @author Mark LaFond
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class XsdSchemaException extends XmlException {

	public XsdSchemaException(String message) {
		super(message);
	}

	public XsdSchemaException(@Nullable String message, Throwable throwable) {
		super(message, throwable);
	}

}
