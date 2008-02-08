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

package org.springframework.ws.soap.security.wss4j.callback.acegi;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.UserCache;
import org.acegisecurity.providers.dao.cache.NullUserCache;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.wss4j.callback.AbstractWss4jCallbackHandler;

/** @author Tareq Abed Rabbo */
public class AcegiCallbackHandler extends AbstractWss4jCallbackHandler {

    private AuthenticationManager authenticationManager;

    private UserCache userCache = new NullUserCache();

    private UserDetailsService userDetailsService;

    public UserCache getUserCache() {
        return userCache;
    }

    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        if (isPasswordPlainTextRequired()) {
            Assert.notNull(authenticationManager, "authenticationManager is required");
        }

        if (isPasswordDigestRequired()) {
            Assert
                    .notNull(userDetailsService, "userDetailsService is required");
        }

    }

    protected void validateUsernameTokenPlainText(WSPasswordCallback callback) throws WSSecurityException {
        if (isPasswordPlainTextRequired()) {
            Assert
                    .notNull(authenticationManager,
                            "authenticationManager is required to validate a usernameToken with a plain text password");
        }
        try {
            Authentication authResult = authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(callback.getIdentifer(), callback.getPassword()));
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Authentication success: " + authResult.toString());
            }
            SecurityContextHolder.getContext().setAuthentication(authResult);
        }
        catch (AuthenticationException failed) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication request for user '" + callback.getIdentifer() + "' failed: " +
                        failed.toString());
            }
            SecurityContextHolder.getContext().setAuthentication(null);
            throw new WSSecurityException(WSSecurityException.FAILURE);
        }
    }

    protected void validateUsernameTokenDigest(WSPasswordCallback callback) throws WSSecurityException {
        if (isPasswordDigestRequired()) {
            Assert
                    .notNull(userDetailsService,
                            "userDetailsService is required to validate a usernameToken with a digest password");
        }
        UserDetails user = loadUserDetails(callback.getIdentifer());
        if (user != null) {
            callback.setPassword(user.getPassword());
        }
    }

    private UserDetails loadUserDetails(String username) throws DataAccessException {
        UserDetails user = userCache.getUserFromCache(username);

        if (user == null) {
            try {
                user = userDetailsService.loadUserByUsername(username);
            }
            catch (UsernameNotFoundException notFound) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Username '" + username + "' not found");
                }
                return null;
            }
            userCache.putUserInCache(user);
        }
        return user;
    }
}
