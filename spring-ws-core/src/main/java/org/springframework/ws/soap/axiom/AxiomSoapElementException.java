/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapElementException;

/**
 * Axiom-specific {@link SoapElementException}.
 *
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class AxiomSoapElementException extends SoapElementException {

	public AxiomSoapElementException(String msg) {
		super(msg);
	}

	public AxiomSoapElementException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AxiomSoapElementException(Throwable ex) {
		super(ex);
	}

}
