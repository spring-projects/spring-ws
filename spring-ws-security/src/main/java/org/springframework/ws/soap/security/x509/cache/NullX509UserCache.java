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
 * "Cache" that doesn't do any caching.
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 */
public class NullX509UserCache implements X509UserCache {

	// ~ Methods
	// ========================================================================================================

	@Override
	public @Nullable UserDetails getUserFromCache(X509Certificate certificate) {
		return null;
	}

	@Override
	public void putUserInCache(X509Certificate certificate, UserDetails user) {
	}

	@Override
	public void removeUserFromCache(X509Certificate certificate) {
	}

}
