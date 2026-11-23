/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml.xpath;

/**
 * Exception throws when a XPath expression cannot be parsed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class XPathParseException extends XPathException {

	/**
	 * Constructs a new instance of the {@code XPathParseException} with the specific
	 * detail message.
	 * @param message the detail message
	 */
	public XPathParseException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of the {@code XPathParseException} with the specific
	 * detail message and exception.
	 * @param message the detail message
	 * @param throwable the wrapped exception
	 */
	public XPathParseException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
