/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.WebServiceException;

/**
 * Exception indicating that something went wrong during WS-Security executions. Has
 * specific subclasses for securement and validation.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class WsSecurityException extends WebServiceException {

	public WsSecurityException(String msg) {
		super(msg);
	}

	public WsSecurityException(@Nullable String msg, Throwable ex) {
		super(msg, ex);
	}

}
