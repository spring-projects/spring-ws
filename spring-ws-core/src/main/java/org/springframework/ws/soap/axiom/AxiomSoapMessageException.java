/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapMessageException;

/**
 * Axiom-specific {@link SoapMessageException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class AxiomSoapMessageException extends SoapMessageException {

	public AxiomSoapMessageException(String msg) {
		super(msg);
	}

	public AxiomSoapMessageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
