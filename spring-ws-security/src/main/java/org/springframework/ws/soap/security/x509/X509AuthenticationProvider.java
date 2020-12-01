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

package org.springframework.ws.soap.security.x509;

import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.x509.cache.NullX509UserCache;
import org.springframework.ws.soap.security.x509.cache.X509UserCache;

/**
 * Processes an X.509 authentication request.
 * <p>
 * Migrated from Spring Security 2 since it has been removed in Spring Security 3.
 * </p>
 *
 * @author Luke Taylor
 * @version $Id: X509AuthenticationProvider.java 3256 2008-08-18 18:20:48Z luke_t $
 */
public class X509AuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {
	// ~ Static fields/initializers =====================================================================================

	private static final Log logger = LogFactory.getLog(X509AuthenticationProvider.class);

	// ~ Instance fields ================================================================================================

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	private X509AuthoritiesPopulator x509AuthoritiesPopulator;
	private X509UserCache userCache = new NullX509UserCache();

	// ~ Methods ========================================================================================================

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(userCache, "An x509UserCache must be set");
		Assert.notNull(x509AuthoritiesPopulator, "An X509AuthoritiesPopulator must be set");
		Assert.notNull(this.messages, "A message source must be set");
	}

	/**
	 * If the supplied authentication token contains a certificate then this will be passed to the configured
	 * {@link X509AuthoritiesPopulator} to obtain the user details and authorities for the user identified by the
	 * certificate.
	 * <p>
	 * If no certificate is present (for example, if the filter is applied to an HttpRequest for which client
	 * authentication hasn't been configured in the container) then a BadCredentialsException will be raised.
	 * </p>
	 *
	 * @param authentication the authentication request.
	 * @return an X509AuthenticationToken containing the authorities of the principal represented by the certificate.
	 * @throws AuthenticationException if the {@link X509AuthoritiesPopulator} rejects the certficate.
	 * @throws BadCredentialsException if no certificate was presented in the authentication request.
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!supports(authentication.getClass())) {
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("X509 authentication request: " + authentication);
		}

		X509Certificate clientCertificate = (X509Certificate) authentication.getCredentials();

		if (clientCertificate == null) {
			throw new BadCredentialsException(
					messages.getMessage("X509AuthenticationProvider.certificateNull", "Certificate is null"));
		}

		UserDetails user = userCache.getUserFromCache(clientCertificate);

		if (user == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Authenticating with certificate " + clientCertificate);
			}
			user = x509AuthoritiesPopulator.getUserDetails(clientCertificate);
			userCache.putUserInCache(clientCertificate, user);
		}

		X509AuthenticationToken result = new X509AuthenticationToken(user, clientCertificate, user.getAuthorities());

		result.setDetails(authentication.getDetails());

		return result;
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

	public void setX509AuthoritiesPopulator(X509AuthoritiesPopulator x509AuthoritiesPopulator) {
		this.x509AuthoritiesPopulator = x509AuthoritiesPopulator;
	}

	public void setX509UserCache(X509UserCache cache) {
		this.userCache = cache;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return X509AuthenticationToken.class.isAssignableFrom(authentication);
	}
}
