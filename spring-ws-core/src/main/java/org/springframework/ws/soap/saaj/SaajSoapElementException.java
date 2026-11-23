/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.saaj;

import org.springframework.ws.soap.SoapElementException;

/**
 * A SAAJ {@link SoapElementException}.
 *
 * @author Arjen Poutsma
 */
@SuppressWarnings("serial")
public class SaajSoapElementException extends SoapElementException {

	public SaajSoapElementException(String msg) {
		super(msg);
	}

	public SaajSoapElementException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public SaajSoapElementException(Throwable ex) {
		super(ex);
	}

}
