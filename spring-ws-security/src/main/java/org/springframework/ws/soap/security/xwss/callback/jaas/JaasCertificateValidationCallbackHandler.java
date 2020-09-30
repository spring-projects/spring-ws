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

package org.springframework.ws.soap.security.xwss.callback.jaas;

import java.security.cert.X509Certificate;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;

/**
 * Provides basic support for integrating with JAAS and certificates. Requires the {@code loginContextName} to be
 * set.Requires a {@code LoginContext} which handles {@code X500Principal}s.
 * <p>
 * This class only handles {@code CertificateValidationCallback}s, and throws an {@code UnsupportedCallbackException}
 * for others.
 *
 * @author Arjen Poutsma
 * @see javax.security.auth.x500.X500Principal
 * @see #setLoginContextName(String)
 * @since 1.0.0
 */
public class JaasCertificateValidationCallbackHandler extends AbstractJaasValidationCallbackHandler {

	/**
	 * Handles {@code CertificateValidationCallback}s, and throws an {@code UnsupportedCallbackException} for others
	 *
	 * @throws UnsupportedCallbackException when the callback is not supported
	 */
	@Override
	protected final void handleInternal(Callback callback) throws UnsupportedCallbackException {
		if (callback instanceof CertificateValidationCallback) {
			((CertificateValidationCallback) callback).setValidator(new JaasCertificateValidator());
		} else {
			throw new UnsupportedCallbackException(callback);
		}
	}

	private class JaasCertificateValidator implements CertificateValidationCallback.CertificateValidator {

		@Override
		public boolean validate(X509Certificate certificate)
				throws CertificateValidationCallback.CertificateValidationException {
			Subject subject = new Subject();
			subject.getPrincipals().add(certificate.getSubjectX500Principal());
			LoginContext loginContext;
			try {
				loginContext = new LoginContext(getLoginContextName(), subject);
			} catch (LoginException ex) {
				throw new CertificateValidationCallback.CertificateValidationException(ex);
			} catch (SecurityException ex) {
				throw new CertificateValidationCallback.CertificateValidationException(ex);
			}

			try {
				loginContext.login();
				Subject subj = loginContext.getSubject();
				if (!subj.getPrincipals().isEmpty()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Authentication request for certificate with DN ["
								+ certificate.getSubjectX500Principal().getName() + "] successful");
					}
					return true;
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Authentication request for certificate with DN ["
								+ certificate.getSubjectX500Principal().getName() + "] failed");
					}
					return false;
				}
			} catch (LoginException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Authentication request for certificate with DN ["
							+ certificate.getSubjectX500Principal().getName() + "] failed");
				}
				return false;
			}
		}
	}
}
