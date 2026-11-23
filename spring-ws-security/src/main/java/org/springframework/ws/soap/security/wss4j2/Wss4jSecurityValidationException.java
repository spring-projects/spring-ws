/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.security.WsSecurityValidationException;

/**
 * WSS4J-specific version of the {@link WsSecurityValidationException}.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @since 2.3.0
 */
@SuppressWarnings("serial")
public class Wss4jSecurityValidationException extends WsSecurityValidationException {

	public Wss4jSecurityValidationException(String msg) {
		super(msg);
	}

	public Wss4jSecurityValidationException(@Nullable String msg, Throwable ex) {
		super(msg, ex);
	}

}
