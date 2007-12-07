/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract implementation of a <code>CallbackHandler</code>.
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
     * Iterates over the given callbacks, and calls <code>handleInternal</code> for each of them.
     *
     * @param callbacks the callbacks
     * @see #handleInternal(javax.security.auth.callback.Callback)
     */
    public final void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            handleInternal(callbacks[i]);
        }
    }

    /** Template method that should be implemented by subclasses. */
    protected abstract void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException;
}
