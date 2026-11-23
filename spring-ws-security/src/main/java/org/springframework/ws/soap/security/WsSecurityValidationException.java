/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security;

import org.jspecify.annotations.Nullable;

/**
 * Exception indicating that something went wrong during the validation of a message.
 * <p>
 * This is a checked exception since we want it to be caught, logged and handled rather
 * than cause the application to fail. Failure to validate a message is usually not a
 * fatal problem.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WsSecurityValidationException extends WsSecurityException {

	public WsSecurityValidationException(String msg) {
		super(msg);
	}

	public WsSecurityValidationException(@Nullable String msg, Throwable ex) {
		super(msg, ex);
	}

}
