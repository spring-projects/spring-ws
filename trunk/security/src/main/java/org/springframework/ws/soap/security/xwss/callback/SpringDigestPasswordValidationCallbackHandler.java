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

package org.springframework.ws.soap.security.xwss.callback;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;

import org.springframework.dao.DataAccessException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.dao.cache.NullUserCache;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

/**
 * Callback handler that validates a password digest using an Spring Security <code>UserDetailsService</code>. Logic
 * based on Spring Security's <code>DigestProcessingFilter</code>.
 * <p/>
 * An Spring Security <code>UserDetailService</code> is used to load <code>UserDetails</code> from. The digest of the
 * password contained in this details object is then compared with the digest in the message.
 * <p/>
 * This class only handles <code>PasswordValidationCallback</code>s that contain a <code>DigestPasswordRequest</code>,
 * and throws an <code>UnsupportedCallbackException</code> for others.
 *
 * @author Arjen Poutsma
 * @see org.springframework.security.userdetails.UserDetailsService
 * @see com.sun.xml.wss.impl.callback.PasswordValidationCallback
 * @see com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest
 * @see org.springframework.security.ui.digestauth.DigestProcessingFilter
 * @since 1.5.0
 */
public class SpringDigestPasswordValidationCallbackHandler extends AbstractCallbackHandler {

    private UserCache userCache = new NullUserCache();

    private UserDetailsService userDetailsService;

    /** Sets the users cache. Not required, but can benefit performance. */
    public void setUserCache(UserCache userCache) {
        this.userCache = userCache;
    }

    /** Sets the Spring Security user details service. Required. */
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(userDetailsService, "userDetailsService is required");
    }

    /**
     * Handles <code>PasswordValidationCallback</code>s that contain a <code>DigestPasswordRequest</code>, and throws an
     * <code>UnsupportedCallbackException</code> for others
     *
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     *          when the callback is not supported
     */
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback passwordCallback = (PasswordValidationCallback) callback;
            if (passwordCallback.getRequest() instanceof PasswordValidationCallback.DigestPasswordRequest) {
                PasswordValidationCallback.DigestPasswordRequest request =
                        (PasswordValidationCallback.DigestPasswordRequest) passwordCallback.getRequest();
                String username = request.getUsername();
                UserDetails user = loadUserDetails(username);
                if (user != null) {
                    request.setPassword(user.getPassword());
                }
                SpringSecurityDigestPasswordValidator validator = new SpringSecurityDigestPasswordValidator(user);
                passwordCallback.setValidator(validator);
                return;
            }
        }
        else if (callback instanceof TimestampValidationCallback) {
            TimestampValidationCallback timestampCallback = (TimestampValidationCallback) callback;
            timestampCallback.setValidator(new DefaultTimestampValidator());

        }
        else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
            return;
        }
        throw new UnsupportedCallbackException(callback);
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

    private class SpringSecurityDigestPasswordValidator extends PasswordValidationCallback.DigestPasswordValidator {

        private UserDetails user;

        private SpringSecurityDigestPasswordValidator(UserDetails user) {
            this.user = user;
        }

        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            if (super.validate(request)) {
                UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(user, user.getPassword());
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication success: " + authRequest.toString());
                }

                SecurityContextHolder.getContext().setAuthentication(authRequest);
                return true;
            }
            else {
                return false;
            }
        }
    }

}