/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.transport.TransportException;

/**
 * Exception thrown whenever a transport error occurs on the client-side.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class WebServiceTransportException extends WebServiceIOException {

	/**
	 * Create a new instance of the {@code WebServiceTransportException} class.
	 * @param msg the detail message
	 */
	public WebServiceTransportException(@Nullable String msg) {
		super(msg);
	}

	/**
	 * Create a new instance of the {@code WebServiceTransportException} class.
	 * @param msg the detail message
	 * @param ex the root {@link TransportException}
	 */
	public WebServiceTransportException(String msg, TransportException ex) {
		super(msg, ex);
	}

}
