/*
 * Copyright 2005-2010 the original author or authors.
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

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Provides a cache of {@link UserDetails} objects for the
 * {@link org.springframework.ws.soap.security.x509.X509AuthenticationProvider}.
 * <p>
 * Similar in function to the {@link org.springframework.security.core.userdetails.UserCache} used by the Dao provider,
 * but the cache is keyed with the user's certificate rather than the user name.
 * </p>
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 */
public interface X509UserCache {
	// ~ Methods ========================================================================================================

	UserDetails getUserFromCache(X509Certificate userCertificate);

	void putUserInCache(X509Certificate key, UserDetails user);

	void removeUserFromCache(X509Certificate key);
}
