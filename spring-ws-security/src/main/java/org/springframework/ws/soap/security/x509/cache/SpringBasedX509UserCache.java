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

package org.springframework.ws.soap.security.x509.cache;

import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;


/**
 * Caches {@code User} objects using a Spring Framework-based {@link Cache}.
 *
 * <p>Migrated from Spring Security 2 since it has been removed in Spring Security 3.</p>
 *
 * @author Luke Taylor
 * @author Ben Alex
 * @author Greg Turnquist
 */
public class SpringBasedX509UserCache implements X509UserCache, InitializingBean {

	private static final Log logger = LogFactory.getLog(SpringBasedX509UserCache.class);

	private Cache cache;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(cache, "cache is mandatory");
	}

	@Override
	public UserDetails getUserFromCache(X509Certificate userCert) {

		if (logger.isDebugEnabled()) {

			String subjectDN = "unknown";

			if ((userCert != null) && (userCert.getSubjectDN() != null)) {
				subjectDN = userCert.getSubjectDN().toString();
			}

			logger.debug("X.509 Cache hit. SubjectDN: " + subjectDN);
		}

		return cache.get(userCert, UserDetails.class);
	}

	@Override
	public void putUserInCache(X509Certificate userCert, UserDetails user) {

		if (logger.isDebugEnabled()) {
			logger.debug("Cache put: " + userCert.getSubjectDN());
		}

		cache.put(userCert, user);
	}

	@Override
	public void removeUserFromCache(X509Certificate userCert) {

		if (logger.isDebugEnabled()) {
			logger.debug("Cache remove: " + userCert.getSubjectDN());
		}

		cache.evict(userCert);
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
