/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.axiom;

import org.springframework.ws.soap.SoapFaultException;

/**
 * Axiom-specific {@link SoapFaultException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class AxiomSoapFaultException extends SoapFaultException {

	public AxiomSoapFaultException(String msg) {
		super(msg);
	}

	public AxiomSoapFaultException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public AxiomSoapFaultException(Throwable ex) {
		super(ex);
	}

}
