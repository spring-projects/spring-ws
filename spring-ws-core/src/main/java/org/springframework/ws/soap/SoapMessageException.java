/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

import org.springframework.ws.WebServiceMessageException;

/**
 * Base class for all SOAP message exceptions.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class SoapMessageException extends WebServiceMessageException {

	/** Constructor for {@code SoapMessageException}. */
	public SoapMessageException(String msg) {
		super(msg);
	}

	/** Constructor for {@code SoapMessageException}. */
	public SoapMessageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
