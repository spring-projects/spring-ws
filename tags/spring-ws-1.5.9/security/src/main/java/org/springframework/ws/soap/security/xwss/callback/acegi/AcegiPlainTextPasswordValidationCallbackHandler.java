/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback.acegi;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

/**
 * Callback handler that validates a certificate uses an Acegi <code>AuthenticationManager</code>. Logic based on
 * Acegi's <code>BasicProcessingFilter</code>.
 * <p/>
 * This handler requires an Acegi <code>AuthenticationManager</code> to operate. It can be set using the
 * <code>authenticationManager</code> property. An Acegi <code>UsernamePasswordAuthenticationToken</code> is created
 * with the username as principal and password as credentials.
 * <p/>
 * This class only handles <code>PasswordValidationCallback</code>s that contain a
 * <code>PlainTextPasswordRequest</code>, and throws an <code>UnsupportedCallbackException</code> for others.
 *
 * @author Arjen Poutsma
 * @see UsernamePasswordAuthenticationToken
 * @see PasswordValidationCallback
 * @see com.sun.xml.wss.impl.callback.PasswordValidationCallback.PlainTextPasswordRequest
 * @see org.acegisecurity.ui.basicauth.BasicProcessingFilter
 * @since 1.0.0
 * @deprecated As of Spring-WS 1.5, in favor of Spring Security
 */
public class AcegiPlainTextPasswordValidationCallbackHandler extends AbstractCallbackHandler
        implements InitializingBean {

    private AuthenticationManager authenticationManager;

    private boolean ignoreFailure = false;

    /** Sets the Acegi authentication manager. Required. */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    /**
     * Handles <code>PasswordValidationCallback</code>s that contain a <code>PlainTextPasswordRequest</code>, and throws
     * an <code>UnsupportedCallbackException</code> for others.
     *
     * @throws UnsupportedCallbackException when the callback is not supported
     */
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
            if (validationCallback.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                validationCallback.setValidator(new AcegiPlainTextPasswordValidator());
                return;
            }
        }
        else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
            return;
        }
        throw new UnsupportedCallbackException(callback);
    }

    private class AcegiPlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                    (PasswordValidationCallback.PlainTextPasswordRequest) request;
            try {
                Authentication authResult = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(plainTextRequest.getUsername(),
                                plainTextRequest.getPassword()));
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication success: " + authResult.toString());
                }
                SecurityContextHolder.getContext().setAuthentication(authResult);
                return true;
            }
            catch (AuthenticationException failed) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for user '" + plainTextRequest.getUsername() + "' failed: " +
                            failed.toString());
                }
                SecurityContextHolder.clearContext();
                return ignoreFailure;
            }
        }
    }

}
