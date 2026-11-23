/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.security.x509;

import java.security.cert.X509Certificate;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Populates the {@code UserDetails} associated with the X.509 certificate presented by a
 * client.
 * <p>
 * Although the certificate will already have been validated by the web container,
 * implementations may choose to perform additional application-specific checks on the
 * certificate content here. If an implementation chooses to reject the certificate, it
 * should throw a
 * {@link org.springframework.security.authentication.BadCredentialsException}.
 * </p>
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 */
public interface X509AuthoritiesPopulator {

	// ~ Methods
	// ========================================================================================================

	/**
	 * Obtains the granted authorities for the specified user.
	 * <p>
	 * May throw any {@code AuthenticationException} or return {@code null} if the
	 * authorities are unavailable.
	 * </p>
	 * @param userCertificate the X.509 certificate supplied
	 * @return the details of the indicated user (at minimum the granted authorities and
	 * the username)
	 * @throws AuthenticationException if the user details are not available or the
	 * certificate isn't valid for the application's purpose.
	 */
	UserDetails getUserDetails(X509Certificate userCertificate) throws AuthenticationException;

}
