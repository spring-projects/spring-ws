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

package org.springframework.ws.soap.security.xwss.callback;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import org.springframework.ws.soap.security.x509.X509AuthenticationToken;

/**
 * Callback handler that validates a certificate using an Spring Security {@code AuthenticationManager}. Logic
 * based on Spring Security's {@code X509ProcessingFilter}.
 *
 * <p>Spring Security {@code X509AuthenticationToken} is created with the certificate as the credentials.
 *
 * <p>The configured authentication manager is expected to supply a provider which can handle this token (usually an instance of
 * {@code X509AuthenticationProvider}).</p>
 *
 * <p>This class only handles {@code CertificateValidationCallback}s, and throws an
 * {@code UnsupportedCallbackException} for others.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.security.x509.X509AuthenticationToken
 * @see org.springframework.ws.soap.security.x509.X509AuthenticationProvider
 * @see com.sun.xml.wss.impl.callback.CertificateValidationCallback
 * @since 1.5.0
 */
public class SpringCertificateValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private AuthenticationManager authenticationManager;

    private boolean ignoreFailure = false;

    /** Sets the Spring Security authentication manager. Required. */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    /**
     * Handles  {@code CertificateValidationCallback}s, and throws an {@code UnsupportedCallbackException} for
     * others
     *
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     *          when the callback is not supported
     */
    @Override
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof CertificateValidationCallback) {
            ((CertificateValidationCallback) callback).setValidator(new SpringSecurityCertificateValidator());
        }
        else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
        }
        else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    private class SpringSecurityCertificateValidator implements CertificateValidationCallback.CertificateValidator {

        @Override
        public boolean validate(X509Certificate certificate)
                throws CertificateValidationCallback.CertificateValidationException {
            boolean result;
            try {
                Authentication authResult =
                        authenticationManager.authenticate(new X509AuthenticationToken(certificate));
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for certificate with DN [" +
                            certificate.getSubjectX500Principal().getName() + "] successful");
                }
                SecurityContextHolder.getContext().setAuthentication(authResult);
                return true;
            }
            catch (AuthenticationException failed) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for certificate with DN [" +
                            certificate.getSubjectX500Principal().getName() + "] failed: " + failed.toString());
                }
                SecurityContextHolder.clearContext();
                result = ignoreFailure;
            }
            return result;
        }
    }
}
