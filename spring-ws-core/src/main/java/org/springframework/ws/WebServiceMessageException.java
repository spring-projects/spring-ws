/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

/**
 * Base class for all web service message exceptions.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WebServiceMessageException extends WebServiceException {

	/** Constructor for {@code WebServiceMessageException}. */
	public WebServiceMessageException(String msg) {
		super(msg);
	}

	/** Constructor for {@code WebServiceMessageException}. */
	public WebServiceMessageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
