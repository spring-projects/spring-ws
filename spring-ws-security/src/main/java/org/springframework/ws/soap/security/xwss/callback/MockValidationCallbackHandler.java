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
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

/**
 * Mock implementation of of callback handler that accepts all password and certificate validation callbacks.
 * <p/>
 * If the <code>valid</code> property is set to <code>true</code> (the default), this handler simply accepts and
 * validates every password or certificate validation callback that is passed to it.
 * <p/>
 * This class handles <code>CertificateValidationCallback</code>s and <code>PasswordValidationCallback</code>s, and
 * throws an <code>UnsupportedCallbackException</code> for others
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class MockValidationCallbackHandler extends AbstractCallbackHandler {

    private boolean isValid = true;

    public MockValidationCallbackHandler() {
    }

    public MockValidationCallbackHandler(boolean valid) {
        isValid = valid;
    }

    @Override
    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof CertificateValidationCallback) {
            CertificateValidationCallback validationCallback = (CertificateValidationCallback) callback;
            validationCallback.setValidator(new MockCertificateValidator());
        }
        else if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
            validationCallback.setValidator(new MockPasswordValidator());
        }
        else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    private class MockCertificateValidator implements CertificateValidationCallback.CertificateValidator {

        @Override
        public boolean validate(X509Certificate certificate)
                throws CertificateValidationCallback.CertificateValidationException {
            return isValid;
        }
    }

    private class MockPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        @Override
        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            return isValid;
        }
    }
}
