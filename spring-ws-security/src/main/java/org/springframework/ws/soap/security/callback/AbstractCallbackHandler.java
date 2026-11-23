/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.callback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract implementation of a {@code CallbackHandler}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractCallbackHandler implements CallbackHandler {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	protected AbstractCallbackHandler() {
	}

	/**
	 * Iterates over the given callbacks, and calls {@code handleInternal} for each of
	 * them.
	 * @param callbacks the callbacks
	 * @see #handleInternal(javax.security.auth.callback.Callback)
	 */
	@Override
	public final void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			handleInternal(callback);
		}
	}

	/** Template method that should be implemented by subclasses. */
	protected abstract void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException;

}
