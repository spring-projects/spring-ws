/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapEnvelopeException;

/**
 * Axiom-specific {@link SoapEnvelopeException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class AxiomSoapEnvelopeException extends SoapEnvelopeException {

	public AxiomSoapEnvelopeException(String msg) {
		super(msg);
	}

	public AxiomSoapEnvelopeException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AxiomSoapEnvelopeException(Throwable ex) {
		super(ex);
	}

}
