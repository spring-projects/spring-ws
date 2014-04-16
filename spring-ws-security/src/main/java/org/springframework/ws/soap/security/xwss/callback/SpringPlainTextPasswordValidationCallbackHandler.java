/*
 * Copyright 2005-2014 the original author or authors.
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
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

/**
 * Callback handler that validates a certificate uses an Spring Security {@code AuthenticationManager}. Logic based
 * on Spring Security's {@code BasicProcessingFilter}.
 *
 * <p>This handler requires an Spring Security {@code AuthenticationManager} to operate. It can be set using the
 * {@code authenticationManager} property. An Spring Security {@code UsernamePasswordAuthenticationToken} is
 * created with the username as principal and password as credentials.
 *
 * <p>This class only handles {@code PasswordValidationCallback}s that contain a
 * {@code PlainTextPasswordRequest}, and throws an {@code UnsupportedCallbackException} for others.
 *
 * @author Arjen Poutsma
 * @see org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 * @see com.sun.xml.wss.impl.callback.PasswordValidationCallback
 * @see com.sun.xml.wss.impl.callback.PasswordValidationCallback.PlainTextPasswordRequest
 * @since 1.5.0
 */
public class SpringPlainTextPasswordValidationCallbackHandler extends AbstractCallbackHandler
        implements InitializingBean {

    private AuthenticationManager authenticationManager;

    private boolean ignoreFailure = false;

    /** Sets the Spring Security authentication manager. Required. */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    /**
     * Handles {@code PasswordValidationCallback}s that contain a {@code PlainTextPasswordRequest}, and throws
     * an {@code UnsupportedCallbackException} for others.
     *
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     *          when the callback is not supported
     */
    @Override
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
            if (validationCallback.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                validationCallback.setValidator(new SpringSecurityPlainTextPasswordValidator());
                return;
            }
        }
        else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
            return;
        }
        throw new UnsupportedCallbackException(callback);
    }

    private class SpringSecurityPlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        @Override
        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                    (PasswordValidationCallback.PlainTextPasswordRequest) request;
            try {
                Authentication authResult = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                        plainTextRequest.getUsername(), plainTextRequest.getPassword()));
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
