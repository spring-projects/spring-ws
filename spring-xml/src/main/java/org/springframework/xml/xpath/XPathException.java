/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

import org.jspecify.annotations.Nullable;

import org.springframework.xml.XmlException;

/**
 * Exception thrown when an error occurs during XPath processing.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class XPathException extends XmlException {

	/**
	 * Constructs a new instance of the {@code XPathException} with the specific detail
	 * message.
	 * @param message the detail message
	 */
	public XPathException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of the {@code XPathException} with the specific detail
	 * message and exception.
	 * @param message the detail message
	 * @param throwable the wrapped exception
	 */
	public XPathException(@Nullable String message, Throwable throwable) {
		super(message, throwable);
	}

}
