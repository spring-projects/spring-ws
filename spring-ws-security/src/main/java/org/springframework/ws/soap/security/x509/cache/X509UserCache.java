/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.x509.cache;

import java.security.cert.X509Certificate;

import org.jspecify.annotations.Nullable;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Provides a cache of {@link UserDetails} objects for the
 * {@link org.springframework.ws.soap.security.x509.X509AuthenticationProvider}.
 * <p>
 * Similar in function to the
 * {@link org.springframework.security.core.userdetails.UserCache} used by the Dao
 * provider, but the cache is keyed with the user's certificate rather than the user name.
 * </p>
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 */
public interface X509UserCache {

	// ~ Methods
	// ========================================================================================================

	@Nullable UserDetails getUserFromCache(X509Certificate userCertificate);

	void putUserInCache(X509Certificate key, UserDetails user);

	void removeUserFromCache(X509Certificate key);

}
