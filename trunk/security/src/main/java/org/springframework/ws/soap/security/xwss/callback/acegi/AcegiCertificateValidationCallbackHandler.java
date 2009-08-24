/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.ws.soap.security.xwss.callback.acegi;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.x509.X509AuthenticationToken;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

/**
 * Callback handler that validates a certificate using an Acegi <code>AuthenticationManager</code>. Logic based on
 * Acegi's <code>X509ProcessingFilter</code>. <p/> An Acegi <code>X509AuthenticationToken</code> is created with the
 * certificate as the credentials. <p/> The configured authentication manager is expected to supply a provider which can
 * handle this token (usually an instance of <code>X509AuthenticationProvider</code>).</p>
 * <p/>
 * This class only handles <code>CertificateValidationCallback</code>s, and throws an
 * <code>UnsupportedCallbackException</code> for others.
 *
 * @author Arjen Poutsma
 * @see X509AuthenticationToken
 * @see org.acegisecurity.providers.x509.X509AuthenticationProvider
 * @see org.acegisecurity.ui.x509.X509ProcessingFilter
 * @see CertificateValidationCallback
 * @since 1.0.0
 * @deprecated As of Spring-WS 1.5, in favor of Spring Security
 */
public class AcegiCertificateValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private AuthenticationManager authenticationManager;

    private boolean ignoreFailure = false;

    /** Sets the Acegi authentication manager. Required. */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    /**
     * Handles  <code>CertificateValidationCallback</code>s, and throws an <code>UnsupportedCallbackException</code> for
     * others
     *
     * @throws UnsupportedCallbackException when the callback is not supported
     */
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof CertificateValidationCallback) {
            ((CertificateValidationCallback) callback).setValidator(new AcegiCertificateValidator());
        }
        else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
        }
        else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    private class AcegiCertificateValidator implements CertificateValidationCallback.CertificateValidator {

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
