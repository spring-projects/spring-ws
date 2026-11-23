/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

/**
 * Exception that is thrown when an error occurs in the HTTP transport.
 *
 * @author Arjen Poutsma
 * @since 1.5.8
 */
@SuppressWarnings("serial")
public class HttpsTransportException extends HttpTransportException {

	public HttpsTransportException(String msg) {
		super(msg);
	}

	public HttpsTransportException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
