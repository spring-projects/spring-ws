/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.wss4j2.callback;

import java.io.IOException;

import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.principal.WSUsernameTokenPrincipalImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.support.SpringSecurityUtils;

/**
 * Callback handler that validates a plain text or digest password using an Spring Security {@code UserDetailsService}.
 *
 * <p>An Spring Security {@link UserDetailsService} is used to load {@link UserDetails} from. The digest of the
 * password contained in this details object is then compared with the digest in the message.
 *
 * @author Arjen Poutsma
 * @author Jamin Hitchcock
 * @since 2.3.0
 */
public class SpringSecurityPasswordValidationCallbackHandler extends AbstractWsPasswordCallbackHandler
		implements InitializingBean {

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

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(userDetailsService, "userDetailsService is required");
	}
	


	/**
	 * Invoked when the callback has a {@link WSPasswordCallback#USERNAME_TOKEN} usage.
	 *
	 * <p>This method is invoked when WSS4J needs the password to fill in or to verify a UsernameToken.
	 *
	 * <p>Default implementation throws an {@link UnsupportedCallbackException}.
	 */
	protected void handleUsernameToken(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
		UserDetails user = loadUserDetails(callback.getIdentifier());
		if (user != null) {
			SpringSecurityUtils.checkUserValidity(user);
			callback.setPassword(user.getPassword());
		}
	}

	@Override
	protected void handleUsernameTokenPrincipal(UsernameTokenPrincipalCallback callback)
			throws IOException, UnsupportedCallbackException {
		UserDetails user = loadUserDetails(callback.getPrincipal().getName());
		WSUsernameTokenPrincipalImpl principal = callback.getPrincipal();
		UsernamePasswordAuthenticationToken authRequest =
				new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), user.getAuthorities());
		if (logger.isDebugEnabled()) {
			logger.debug("Authentication success: " + authRequest.toString());
		}
		SecurityContextHolder.getContext().setAuthentication(authRequest);
	}

	@Override
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
