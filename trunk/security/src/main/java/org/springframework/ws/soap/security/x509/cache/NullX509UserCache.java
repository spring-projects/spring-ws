/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.soap.security.x509.cache;

import java.security.cert.X509Certificate;

import org.springframework.security.core.userdetails.UserDetails;


/**
 * "Cache" that doesn't do any caching.
 * <p>Migrated from Spring Security 2 since it has been removed in Spring Security 3.</p>
 *
 * @author Luke Taylor
 */
public class NullX509UserCache implements X509UserCache {
    //~ Methods ========================================================================================================

    public UserDetails getUserFromCache(X509Certificate certificate) {
        return null;
    }

    public void putUserInCache(X509Certificate certificate, UserDetails user) {}

    public void removeUserFromCache(X509Certificate certificate) {}
}
