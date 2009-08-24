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

package org.springframework.ws.soap.security.wss4j.callback.acegi;

import java.io.IOException;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.UserCache;
import org.acegisecurity.providers.dao.cache.NullUserCache;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSUsernameTokenPrincipal;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.wss4j.callback.AbstractWsPasswordCallbackHandler;
import org.springframework.ws.soap.security.wss4j.callback.UsernameTokenPrincipalCallback;
import org.springframework.ws.soap.security.support.AcegiUtils;

/**
 * Callback handler that validates a password digest using an Acegi <code>UserDetailsService</code>. Logic based on
 * Acegi's <code>DigestProcessingFilter</code>.
 * <p/>
 * An Acegi <code>UserDetailService</code> is used to load <code>UserDetails</code> from. The digest of the password
 * contained in this details object is then compared with the digest in the message.
 *
 * @author Arjen Poutsma
 * @see org.acegisecurity.userdetails.UserDetailsService
 * @see org.acegisecurity.ui.digestauth.DigestProcessingFilter
 * @since 1.5.0
 */
public class AcegiDigestPasswordValidationCallbackHandler extends AbstractWsPasswordCallbackHandler
        implements InitializingBean {

    private UserCache userCache = new NullUserCache();

    private UserDetailsService userDetailsService;

    /** Sets the users cache. Not required, but can benefit performance. */
    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }

    /** Sets the Acegi user details service. Required. */
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(userDetailsService, "userDetailsService is required");
    }

    protected void handleUsernameToken(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
        String identifier = callback.getIdentifier();
        UserDetails user = loadUserDetails(identifier);
        if (user != null) {
            AcegiUtils.checkUserValidity(user);
            callback.setPassword(user.getPassword());
        }
    }

    protected void handleUsernameTokenPrincipal(UsernameTokenPrincipalCallback callback)
            throws IOException, UnsupportedCallbackException {
        UserDetails user = loadUserDetails(callback.getPrincipal().getName());
        WSUsernameTokenPrincipal principal = callback.getPrincipal();
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), user.getAuthorities());
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authRequest.toString());
        }
        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

    protected void handleCleanup(CleanupCallback callback) throws IOException, UnsupportedCallbackException {
        SecurityContextHolder.clearContext();
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
