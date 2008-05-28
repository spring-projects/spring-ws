/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.security.wss4j.callback.acegi;

import java.io.IOException;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.wss4j.callback.AbstractWsPasswordCallbackHandler;

/**
 * Callback handler that validates a certificate uses an Acegi <code>AuthenticationManager</code>. Logic based on
 * Acegi's <code>BasicProcessingFilter</code>.
 * <p/>
 * This handler requires an Acegi <code>AuthenticationManager</code> to operate. It can be set using the
 * <code>authenticationManager</code> property. An Acegi <code>UsernamePasswordAuthenticationToken</code> is created
 * with the username as principal and password as credentials.
 *
 * @author Arjen Poutsma
 * @see org.acegisecurity.providers.UsernamePasswordAuthenticationToken
 * @see org.acegisecurity.ui.basicauth.BasicProcessingFilter
 * @since 1.5.0
 */
public class AcegiPlainTextPasswordValidationCallbackHandler extends AbstractWsPasswordCallbackHandler
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

    protected void handleCleanup(CleanupCallback callback) throws IOException, UnsupportedCallbackException {
        SecurityContextHolder.clearContext();
    }

    protected void handleUsernameTokenUnknown(WSPasswordCallback callback)
            throws IOException, UnsupportedCallbackException {
        String identifier = callback.getIdentifer();
        try {
            Authentication authResult = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(identifier, callback.getPassword()));
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication success: " + authResult.toString());
            }
            SecurityContextHolder.getContext().setAuthentication(authResult);
        }
        catch (AuthenticationException failed) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication request for user '" + identifier + "' failed: " + failed.toString());
            }
            SecurityContextHolder.clearContext();
            if (!ignoreFailure) {
                throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
            }
        }
    }
}
