/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapMessageCreationException;

/**
 * Axiom-specific {@link SoapMessageCreationException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class AxiomSoapMessageCreationException extends SoapMessageCreationException {

	public AxiomSoapMessageCreationException(String msg) {
		super(msg);
	}

	public AxiomSoapMessageCreationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
