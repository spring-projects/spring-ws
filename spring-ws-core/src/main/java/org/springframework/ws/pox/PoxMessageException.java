/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.pox;

import org.springframework.ws.WebServiceMessageException;

/**
 * Specific subclass of {@code WebServiceMessageException} for Plain Old XML messages.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class PoxMessageException extends WebServiceMessageException {

	public PoxMessageException(String msg) {
		super(msg);
	}

	public PoxMessageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
