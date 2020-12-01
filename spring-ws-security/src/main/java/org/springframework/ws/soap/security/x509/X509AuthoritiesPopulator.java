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

package org.springframework.ws.soap.security.x509;

import java.security.cert.X509Certificate;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Populates the {@code UserDetails} associated with the X.509 certificate presented by a client.
 * <p>
 * Although the certificate will already have been validated by the web container, implementations may choose to perform
 * additional application-specific checks on the certificate content here. If an implementation chooses to reject the
 * certificate, it should throw a {@link org.springframework.security.authentication.BadCredentialsException}.
 * </p>
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 */
public interface X509AuthoritiesPopulator {
	// ~ Methods ========================================================================================================

	/**
	 * Obtains the granted authorities for the specified user.
	 * <p>
	 * May throw any {@code AuthenticationException} or return {@code null} if the authorities are unavailable.
	 * </p>
	 *
	 * @param userCertificate the X.509 certificate supplied
	 * @return the details of the indicated user (at minimum the granted authorities and the username)
	 * @throws AuthenticationException if the user details are not available or the certificate isn't valid for the
	 *           application's purpose.
	 */
	UserDetails getUserDetails(X509Certificate userCertificate) throws AuthenticationException;
}
