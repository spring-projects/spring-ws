/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.xml;

import org.jspecify.annotations.Nullable;

import org.springframework.core.NestedRuntimeException;

/**
 * Root of the hierarchy of XML exception.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class XmlException extends NestedRuntimeException {

	/**
	 * Constructs a new instance of the {@code XmlException} with the specific detail
	 * message.
	 * @param message the detail message
	 */
	protected XmlException(String message) {
		super(message);
	}

	/**
	 * Constructs a new instance of the {@code XmlException} with the specific detail
	 * message and exception.
	 * @param message the detail message
	 * @param throwable the wrapped exception
	 */
	protected XmlException(@Nullable String message, Throwable throwable) {
		super(message, throwable);
	}

}
