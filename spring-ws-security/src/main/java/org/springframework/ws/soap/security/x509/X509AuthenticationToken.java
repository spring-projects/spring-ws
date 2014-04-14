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

package org.springframework.ws.soap.security.x509;

import java.security.cert.X509Certificate;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;


/**
 * <code>Authentication</code> implementation for X.509 client-certificate authentication.
 * <p>Migrated from Spring Security 2 since it has been removed in Spring Security 3.</p>
 *
 * @author Luke Taylor
 */
public class X509AuthenticationToken extends AbstractAuthenticationToken {
    //~ Instance fields ================================================================================================

    private static final long serialVersionUID = 1L;
    private Object principal;
    private X509Certificate credentials;

    //~ Constructors ===================================================================================================

    /**
     * Used for an authentication request.  The {@link org.springframework.security.core.Authentication#isAuthenticated()} will return
     * <code>false</code>.
     *
     * @param credentials the certificate
     */
    public X509AuthenticationToken(X509Certificate credentials) {
        super(null);
        this.credentials = credentials;
    }

    /**
     * Used for an authentication response object. The {@link org.springframework.security.core.Authentication#isAuthenticated()}
     * will return <code>true</code>.
     *
     * @param principal the principal, which is generally a
     *        <code>UserDetails</code>
     * @param credentials the certificate
     * @param authorities the authorities
     */
    public X509AuthenticationToken(Object principal, X509Certificate credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    //~ Methods ========================================================================================================

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
