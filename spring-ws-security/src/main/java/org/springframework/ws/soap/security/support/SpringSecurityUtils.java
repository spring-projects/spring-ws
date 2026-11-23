/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.support;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * Generic utility methods for Spring Security.
 *
 * @author Tareq Abedrabbo
 * @since 1.5.8
 * @deprecated as of 4.0.19 in favor of {@link AccountStatusUserDetailsChecker}
 */
@Deprecated(since = "4.0.19", forRemoval = true)
public abstract class SpringSecurityUtils {

	private static final UserDetailsChecker ACCOUNT_STATUS_CHECKER = new AccountStatusUserDetailsChecker();

	/**
	 * Checks the validity of a user's account and credentials.
	 * @param user the user to check
	 * @throws AccountExpiredException if the account has expired
	 * @throws CredentialsExpiredException if the credentials have expired
	 * @throws DisabledException if the account is disabled
	 * @throws LockedException if the account is locked
	 * @deprecated as of 4.0.19 in favor of {@link AccountStatusUserDetailsChecker}
	 */
	@Deprecated(since = "4.0.19", forRemoval = true)
	public static void checkUserValidity(UserDetails user)
			throws AccountExpiredException, CredentialsExpiredException, DisabledException, LockedException {
		ACCOUNT_STATUS_CHECKER.check(user);
	}

}
