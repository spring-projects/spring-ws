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
public class SoapEnvelopeException extends SoapMessageException {

	public SoapEnvelopeException(String msg) {
		super(msg);
	}

	public SoapEnvelopeException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SoapEnvelopeException(Throwable ex) {
		super("Could not access envelope: " + ex.getMessage(), ex);
	}

}
