/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapEnvelopeException;

/**
 * Exception thrown when an Axiom SOAP body could not be accessed.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class AxiomSoapBodyException extends SoapEnvelopeException {

	public AxiomSoapBodyException(String msg) {
		super(msg);
	}

	public AxiomSoapBodyException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AxiomSoapBodyException(Throwable ex) {
		super(ex);
	}

}
