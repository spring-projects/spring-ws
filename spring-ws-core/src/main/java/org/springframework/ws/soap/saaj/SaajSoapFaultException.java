/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapFaultException;

/**
 * A SAAJ {@link SoapFaultException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SaajSoapFaultException extends SoapFaultException {

	public SaajSoapFaultException(String msg) {
		super(msg);
	}

	public SaajSoapFaultException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SaajSoapFaultException(Throwable ex) {
		super(ex);
	}

}
