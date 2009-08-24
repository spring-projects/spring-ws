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
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.UserCache;
import org.acegisecurity.providers.dao.cache.NullUserCache;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.support.AcegiUtils;
import org.springframework.ws.soap.security.xwss.callback.DefaultTimestampValidator;

/**
 * Callback handler that validates a password digest using an Acegi <code>UserDetailsService</code>. Logic based on
 * Acegi's <code>DigestProcessingFilter</code>.
 * <p/>
 * An Acegi <code>UserDetailService</code> is used to load <code>UserDetails</code> from. The digest of the password
 * contained in this details object is then compared with the digest in the message.
 * <p/>
 * This class only handles <code>PasswordValidationCallback</code>s that contain a <code>DigestPasswordRequest</code>,
 * and throws an <code>UnsupportedCallbackException</code> for others.
 *
 * @author Arjen Poutsma
 * @see UserDetailsService
 * @see PasswordValidationCallback
 * @see com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest
 * @see org.acegisecurity.ui.digestauth.DigestProcessingFilter
 * @since 1.0.0
 * @deprecated As of Spring-WS 1.5, in favor of Spring Security
 */
public class AcegiDigestPasswordValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

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

    /**
     * Handles <code>PasswordValidationCallback</code>s that contain a <code>DigestPasswordRequest</code>, and throws an
     * <code>UnsupportedCallbackException</code> for others
     *
     * @throws UnsupportedCallbackException when the callback is not supported
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
                    AcegiUtils.checkUserValidity(user);
                    request.setPassword(user.getPassword());
                }
                AcegiDigestPasswordValidator validator = new AcegiDigestPasswordValidator(user);
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

    private class AcegiDigestPasswordValidator extends PasswordValidationCallback.DigestPasswordValidator {

        private UserDetails user;

        private AcegiDigestPasswordValidator(UserDetails user) {
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
