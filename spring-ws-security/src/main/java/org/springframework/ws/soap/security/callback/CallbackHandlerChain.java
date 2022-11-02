/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.callback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Represents a chain of {@code CallbackHandler}s. For each callback, each of the handlers is called in term. If a
 * handler throws a {@code UnsupportedCallbackException}, the next handler is tried.
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
		return callbackHandlers;
	}

	@Override
	protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
		boolean allUnsupported = true;
		for (CallbackHandler callbackHandler : callbackHandlers) {
			try {
				callbackHandler.handle(new Callback[] { callback });
				allUnsupported = false;
			} catch (UnsupportedCallbackException ex) {
				// if an UnsupportedCallbackException occurs, go to the next handler
			}
		}
		if (allUnsupported) {
			throw new UnsupportedCallbackException(callback);
		}
	}
}
