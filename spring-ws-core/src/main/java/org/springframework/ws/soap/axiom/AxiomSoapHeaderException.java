/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapHeaderException;

/**
 * Axiom-specific {@link SoapHeaderException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class AxiomSoapHeaderException extends SoapHeaderException {

	public AxiomSoapHeaderException(String msg) {
		super(msg);
	}

	public AxiomSoapHeaderException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AxiomSoapHeaderException(Throwable ex) {
		super(ex);
	}

}
