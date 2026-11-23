/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.validation;

import org.springframework.xml.XmlException;

/**
 * Exception thrown when a validation error occurs.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class XmlValidationException extends XmlException {

	public XmlValidationException(String s) {
		super(s);
	}

	public XmlValidationException(String s, Throwable throwable) {
		super(s, throwable);
	}

}
