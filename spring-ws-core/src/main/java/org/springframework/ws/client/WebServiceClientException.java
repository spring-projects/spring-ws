/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceException;

/**
 * Exception thrown whenever an error occurs on the client-side.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WebServiceClientException extends WebServiceException {

	/**
	 * Create a new instance of the {@code WebServiceClientException} class.
	 * @param msg the detail message
	 */
	public WebServiceClientException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * Create a new instance of the {@code WebServiceClientException} class.
	 * @param msg the detail message
	 * @param ex the root {@link Throwable exception}
	 */
	public WebServiceClientException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
