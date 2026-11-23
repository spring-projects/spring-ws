/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

/**
 * Exception thrown when a SOAP fault could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapFaultException extends SoapEnvelopeException {

	public SoapFaultException(String msg) {
		super(msg);
	}

	public SoapFaultException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SoapFaultException(Throwable ex) {
		super("Could not access fault: " + ex.getMessage(), ex);
	}

}
