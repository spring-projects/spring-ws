/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

/**
 * Exception thrown when a SOAP header could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapHeaderException extends SoapMessageException {

	public SoapHeaderException(String msg) {
		super(msg);
	}

	public SoapHeaderException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SoapHeaderException(Throwable ex) {
		super("Could not access header: " + ex.getMessage(), ex);
	}

}
