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

package org.springframework.ws.soap.security.xwss.callback.jaas;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

import org.springframework.ws.soap.security.xwss.callback.AbstractCallbackHandler;

/**
 * Provides basic support for integrating with JAAS and plain text passwords.
 * <p/>
 * This class only handles <code>PasswordValidationCallback</code>s that contain a
 * <code>PlainTextPasswordRequest</code>, and throws an <code>UnsupportedCallbackException</code> for others.
 *
 * @author Arjen Poutsma
 * @see #getLoginContextName()
 */
public class JaasPlainTextPasswordValidationCallbackHandler extends AbstractJaasValidationCallbackHandler {

    protected JaasPlainTextPasswordValidationCallbackHandler() {
    }

    /**
     * Handles <code>PasswordValidationCallback</code>s that contain a <code>PlainTextPasswordRequest</code>, and throws
     * an <code>UnsupportedCallbackException</code> for others.
     *
     * @throws UnsupportedCallbackException when the callback is not supported
     */
    protected final void handleInternal(Callback callback) throws UnsupportedCallbackException {
        if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
            if (validationCallback.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                validationCallback.setValidator(new JaasPlainTextPasswordValidator());
                return;
            }
        }
        throw new UnsupportedCallbackException(callback);
    }

    private class JaasPlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                    (PasswordValidationCallback.PlainTextPasswordRequest) request;

            final String username = plainTextRequest.getUsername();
            final String password = plainTextRequest.getPassword();

            LoginContext loginContext = null;
            try {
                loginContext = new LoginContext(getLoginContextName(), new AbstractCallbackHandler() {

                    protected void handleInternal(Callback callback) throws UnsupportedCallbackException {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(username);
                        }
                        else if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        }
                        else {
                            throw new UnsupportedCallbackException(callback);
                        }
                    }
                });
            }
            catch (LoginException ex) {
                throw new PasswordValidationCallback.PasswordValidationException(ex);
            }
            catch (SecurityException ex) {
                throw new PasswordValidationCallback.PasswordValidationException(ex);
            }

            try {
                loginContext.login();
                Subject subject = loginContext.getSubject();
                if (!subject.getPrincipals().isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication request for user '" + username + "' successful");
                    }
                    return true;
                }
                else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Authentication request for user '" + username + "' failed");
                    }
                    return false;
                }
            }
            catch (LoginException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for user '" + username + "' failed");
                }
                return false;
            }
        }


    }
}

