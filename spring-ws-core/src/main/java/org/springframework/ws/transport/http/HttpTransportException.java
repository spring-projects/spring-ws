/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import org.springframework.ws.transport.TransportException;

/**
 * Exception that is thrown when an error occurs in the HTTP transport.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class HttpTransportException extends TransportException {

	public HttpTransportException(String msg) {
		super(msg);
	}

	protected HttpTransportException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}

}
