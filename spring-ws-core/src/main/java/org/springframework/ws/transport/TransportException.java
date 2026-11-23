/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for exceptions related to the transport layer.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public abstract class TransportException extends IOException {

	protected TransportException(@Nullable String msg) {
		super(msg);
	}

	protected TransportException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}

}
