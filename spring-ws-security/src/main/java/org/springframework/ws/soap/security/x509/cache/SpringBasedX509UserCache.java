/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.x509.cache;

import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * Caches {@code User} objects using a Spring Framework-based {@link Cache}.
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 * @author Ben Alex
 * @author Greg Turnquist
 */
public class SpringBasedX509UserCache implements X509UserCache, InitializingBean {

	private static final Log logger = LogFactory.getLog(SpringBasedX509UserCache.class);

	@SuppressWarnings("NullAway.Init")
	private Cache cache;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.cache, "cache is mandatory");
	}

	@Override
	public @Nullable UserDetails getUserFromCache(X509Certificate userCert) {

		if (logger.isDebugEnabled()) {

			String subjectDN = "unknown";

			if (userCert.getSubjectX500Principal() != null) {
				subjectDN = userCert.getSubjectX500Principal().toString();
			}

			logger.debug("X.509 Cache hit. SubjectDN: " + subjectDN);
		}

		return this.cache.get(userCert, UserDetails.class);
	}

	@Override
	public void putUserInCache(X509Certificate userCert, UserDetails user) {

		if (logger.isDebugEnabled()) {
			logger.debug("Cache put: " + userCert.getSubjectX500Principal());
		}

		this.cache.put(userCert, user);
	}

	@Override
	public void removeUserFromCache(X509Certificate userCert) {

		if (logger.isDebugEnabled()) {
			logger.debug("Cache remove: " + userCert.getSubjectX500Principal());
		}

		this.cache.evict(userCert);
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

}
