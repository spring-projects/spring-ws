/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapHeaderException;

/**
 * A SAAJ {@link SoapHeaderException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SaajSoapHeaderException extends SoapHeaderException {

	public SaajSoapHeaderException(String msg) {
		super(msg);
	}

	public SaajSoapHeaderException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SaajSoapHeaderException(Throwable ex) {
		super(ex);
	}

}
