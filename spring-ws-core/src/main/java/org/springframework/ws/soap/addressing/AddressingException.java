/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.addressing;

import org.springframework.ws.WebServiceException;

/**
 * Exception thrown in case on WS-Addressing errors.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class AddressingException extends WebServiceException {

	public AddressingException(String msg) {
		super(msg);
	}

	public AddressingException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
