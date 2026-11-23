/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapBodyException;

/**
 * A SAAJ {@link SoapBodyException}.
 *
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class SaajSoapBodyException extends SoapBodyException {

	public SaajSoapBodyException(String msg) {
		super(msg);
	}

	public SaajSoapBodyException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SaajSoapBodyException(Throwable ex) {
		super(ex);
	}

}
