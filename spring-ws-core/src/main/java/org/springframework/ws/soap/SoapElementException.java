/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

/**
 * Exception thrown when a SOAP element could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapElementException extends SoapMessageException {

	public SoapElementException(String msg) {
		super(msg);
	}

	public SoapElementException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SoapElementException(Throwable ex) {
		super("Could not access element: " + ex.getMessage(), ex);
	}

}
