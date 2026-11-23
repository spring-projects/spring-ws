/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapMessageException;

/**
 * A SAAJ {@link SoapMessageException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SaajSoapMessageException extends SoapMessageException {

	public SaajSoapMessageException(String msg) {
		super(msg);
	}

	public SaajSoapMessageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
