/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapMessageCreationException;

/**
 * A SAAJ {@link SoapMessageCreationException}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SaajSoapMessageCreationException extends SoapMessageCreationException {

	public SaajSoapMessageCreationException(String msg) {
		super(msg);
	}

	public SaajSoapMessageCreationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
