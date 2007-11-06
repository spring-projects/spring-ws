/*
 * Copyright 2006 the original author or authors.
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

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import junit.framework.TestCase;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.acegisecurity.providers.x509.X509AuthenticationToken;
import org.easymock.MockControl;

import org.springframework.core.io.ClassPathResource;

public class AcegiCertificateValidationCallbackHandlerTest extends TestCase {

    private AcegiCertificateValidationCallbackHandler callbackHandler;

    private MockControl control;

    private AuthenticationManager mock;

    private X509Certificate certificate;

    private CertificateValidationCallback callback;

    protected void setUp() throws Exception {
        callbackHandler = new AcegiCertificateValidationCallbackHandler();
        control = MockControl.createControl(AuthenticationManager.class);
        mock = (AuthenticationManager) control.getMock();
        callbackHandler.setAuthenticationManager(mock);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream is = null;
        try {
            is = new ClassPathResource("/org/springframework/ws/soap/security/xwss/test-keystore.jks").getInputStream();
            keyStore.load(is, "password".toCharArray());
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
        certificate = (X509Certificate) keyStore.getCertificate("alias");
        callback = new CertificateValidationCallback(certificate);
    }

    public void testValidateCertificateValid() throws Exception {
        mock.authenticate(new X509AuthenticationToken(certificate));
        control.setMatcher(MockControl.ALWAYS_MATCHER);
        control.setReturnValue(new TestingAuthenticationToken(certificate, null, new GrantedAuthority[0]));
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertTrue("Not authenticated", authenticated);
        control.verify();
    }

    public void testValidateCertificateInvalid() throws Exception {
        mock.authenticate(new X509AuthenticationToken(certificate));
        control.setMatcher(MockControl.ALWAYS_MATCHER);
        control.setThrowable(new BadCredentialsException(""));
        control.replay();
        callbackHandler.handleInternal(callback);
        boolean authenticated = callback.getResult();
        assertFalse("Authenticated", authenticated);
        control.verify();
    }

}