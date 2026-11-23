/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.pox.dom;

import org.springframework.ws.pox.PoxMessageException;

/**
 * Specific subclass of {@code PoxMessageException} for DOM Plain Old XML messages.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public class DomPoxMessageException extends PoxMessageException {

	public DomPoxMessageException(String msg) {
		super(msg);
	}

	public DomPoxMessageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
