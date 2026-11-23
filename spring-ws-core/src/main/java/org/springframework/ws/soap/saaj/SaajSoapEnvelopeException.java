/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapEnvelopeException;

/**
 * A SAAJ {@link SoapEnvelopeException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SaajSoapEnvelopeException extends SoapEnvelopeException {

	public SaajSoapEnvelopeException(String msg) {
		super(msg);
	}

	public SaajSoapEnvelopeException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SaajSoapEnvelopeException(Throwable ex) {
		super(ex);
	}

}
