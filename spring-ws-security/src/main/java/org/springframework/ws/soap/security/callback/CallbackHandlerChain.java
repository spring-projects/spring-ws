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

/**
 * Represents a chain of {@code CallbackHandler}s. For each callback, each of the handlers
 * is called in term. If a handler throws a {@code UnsupportedCallbackException}, the next
 * handler is tried.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class CallbackHandlerChain extends AbstractCallbackHandler {

	private final CallbackHandler[] callbackHandlers;

	public CallbackHandlerChain(CallbackHandler[] callbackHandlers) {
		this.callbackHandlers = callbackHandlers;
	}

	public CallbackHandler[] getCallbackHandlers() {
		return this.callbackHandlers;
	}

	@Override
	protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
		boolean allUnsupported = true;
		for (CallbackHandler callbackHandler : this.callbackHandlers) {
			try {
				callbackHandler.handle(new Callback[] { callback });
				allUnsupported = false;
			}
			catch (UnsupportedCallbackException ex) {
				// if an UnsupportedCallbackException occurs, go to the next handler
			}
		}
		if (allUnsupported) {
			throw new UnsupportedCallbackException(callback);
		}
	}

}
