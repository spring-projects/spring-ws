/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap;

/**
 * Exception thrown when a web service message cannot be created.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class SoapMessageCreationException extends SoapMessageException {

	/** Constructor for {@code SoapMessageCreationException}. */
	public SoapMessageCreationException(String msg) {
		super(msg);
	}

	/** Constructor for {@code SoapMessageCreationException}. */
	public SoapMessageCreationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
