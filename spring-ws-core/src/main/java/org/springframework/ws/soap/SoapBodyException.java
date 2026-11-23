/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

/**
 * Exception thrown when a SOAP body could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapBodyException extends SoapMessageException {

	public SoapBodyException(String msg) {
		super(msg);
	}

	public SoapBodyException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SoapBodyException(Throwable ex) {
		super("Could not access body: " + ex.getMessage(), ex);
	}

}
