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

package org.springframework.ws.soap.security.support;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Generic utility methods for Spring Security
 *
 * @author Tareq Abedrabbo
 * @since 1.5.8
 */
public abstract class SpringSecurityUtils {

	/**
	 * Checks the validity of a user's account and credentials.
	 * @param user the user to check
	 * @throws AccountExpiredException if the account has expired
	 * @throws CredentialsExpiredException if the credentials have expired
	 * @throws DisabledException if the account is disabled
	 * @throws LockedException if the account is locked
	 */
	@SuppressWarnings("deprecation")
	public static void checkUserValidity(UserDetails user)
			throws AccountExpiredException, CredentialsExpiredException, DisabledException, LockedException {
		if (!user.isAccountNonLocked()) {
			throw new LockedException("Account for user '" + user + "' is locked");
		}

		if (!user.isEnabled()) {
			throw new DisabledException("User '" + user + "' is disabled");
		}

		if (!user.isAccountNonExpired()) {
			throw new AccountExpiredException("Account for user '" + user + "' has expired");
		}

		if (!user.isCredentialsNonExpired()) {
			throw new CredentialsExpiredException("Credentials for user '" + user + "' have expired");
		}
	}
}
