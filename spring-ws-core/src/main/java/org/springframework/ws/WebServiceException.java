/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

import org.jspecify.annotations.Nullable;

import org.springframework.core.NestedRuntimeException;

/**
 * Root of the hierarchy of Web Service exceptions.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WebServiceException extends NestedRuntimeException {

	/**
	 * Create a new instance of the {@code WebServiceException} class.
	 * @param msg the detail message
	 */
	public WebServiceException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * Create a new instance of the {@code WebServiceException} class.
	 * @param msg the detail message
	 * @param ex the root {@link Throwable exception}
	 */
	public WebServiceException(@Nullable String msg, Throwable ex) {
		super(msg, ex);
	}

}
