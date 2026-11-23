/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

/**
 * Exception thrown whenever an I/O error occurs on the client-side.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class WebServiceIOException extends WebServiceClientException {

	/**
	 * Create a new instance of the {@code WebServiceIOException} class.
	 * @param msg the detail message
	 */
	public WebServiceIOException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * Create a new instance of the {@code WebServiceIOException} class.
	 * @param msg the detail message
	 * @param ex the root {@link IOException}
	 */
	public WebServiceIOException(String msg, IOException ex) {
		super(msg, ex);
	}

}
