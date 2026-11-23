/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.wss4j2;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.soap.security.WsSecuritySecurementException;

/**
 * WSS4J-specific version of the {@link WsSecuritySecurementException}.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @since 2.3.0
 */
@SuppressWarnings("serial")
public class Wss4jSecuritySecurementException extends WsSecuritySecurementException {

	public Wss4jSecuritySecurementException(String msg) {
		super(msg);
	}

	public Wss4jSecuritySecurementException(@Nullable String msg, Throwable ex) {
		super(msg, ex);
	}

}
