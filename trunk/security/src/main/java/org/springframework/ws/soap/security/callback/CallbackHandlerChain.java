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

package org.springframework.ws.soap.security.callback;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;

/**
 * Represents a chain of <code>CallbackHandler</code>s. For each callback, each of the handlers is called in term. If a
 * handler throws a <code>UnsupportedCallbackException</code>, the next handler is tried.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class CallbackHandlerChain extends AbstractCallbackHandler {

    private CallbackHandler[] callbackHandlers;

    public CallbackHandlerChain(CallbackHandler[] callbackHandlers) {
        this.callbackHandlers = callbackHandlers;
    }

    public void setCallbackHandlers(CallbackHandler[] callbackHandlers) {
        this.callbackHandlers = callbackHandlers;
    }

    protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof CertificateValidationCallback) {
            handleCertificateValidationCallback((CertificateValidationCallback) callback);
        }
        else if (callback instanceof PasswordValidationCallback) {
            handlePasswordValidationCallback((PasswordValidationCallback) callback);
        }
        else if (callback instanceof TimestampValidationCallback) {
            handleTimestampValidationCallback((TimestampValidationCallback) callback);
        }
        else {
            boolean allUnsupported = true;
            for (int i = 0; i < callbackHandlers.length; i++) {
                CallbackHandler callbackHandler = callbackHandlers[i];
                try {
                    callbackHandler.handle(new Callback[]{callback});
                    allUnsupported = false;
                }
                catch (UnsupportedCallbackException ex) {
                    // if an UnsupportedCallbackException occurs, go to the next handler
                }
            }
            if (allUnsupported) {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private void handleCertificateValidationCallback(CertificateValidationCallback callback) {
        callback.setValidator(new CertificateValidatorChain(callback));
    }

    private void handlePasswordValidationCallback(PasswordValidationCallback callback) {
        callback.setValidator(new PasswordValidatorChain(callback));
    }

    private void handleTimestampValidationCallback(TimestampValidationCallback callback) {
        callback.setValidator(new TimestampValidatorChain(callback));
    }

    private class TimestampValidatorChain implements TimestampValidationCallback.TimestampValidator {

        private TimestampValidationCallback callback;

        private TimestampValidatorChain(TimestampValidationCallback callback) {
            this.callback = callback;
        }

        public void validate(TimestampValidationCallback.Request request)
                throws TimestampValidationCallback.TimestampValidationException {
            for (int i = 0; i < callbackHandlers.length; i++) {
                CallbackHandler callbackHandler = callbackHandlers[i];
                try {
                    callbackHandler.handle(new Callback[]{callback});
                    callback.getResult();
                }
                catch (IOException e) {
                    throw new TimestampValidationCallback.TimestampValidationException(e);
                }
                catch (UnsupportedCallbackException e) {
                    // ignore
                }
            }
        }
    }

    private class PasswordValidatorChain implements PasswordValidationCallback.PasswordValidator {

        private PasswordValidationCallback callback;

        private PasswordValidatorChain(PasswordValidationCallback callback) {
            this.callback = callback;
        }

        public boolean validate(PasswordValidationCallback.Request request)
                throws PasswordValidationCallback.PasswordValidationException {
            boolean allUnsupported = true;
            for (int i = 0; i < callbackHandlers.length; i++) {
                CallbackHandler callbackHandler = callbackHandlers[i];
                try {
                    callbackHandler.handle(new Callback[]{callback});
                    allUnsupported = false;
                    if (!callback.getResult()) {
                        return false;
                    }
                }
                catch (IOException e) {
                    throw new PasswordValidationCallback.PasswordValidationException(e);
                }
                catch (UnsupportedCallbackException e) {
                    // ignore
                }
            }
            return !allUnsupported;
        }
    }

    private class CertificateValidatorChain implements CertificateValidationCallback.CertificateValidator {

        private CertificateValidationCallback callback;

        private CertificateValidatorChain(CertificateValidationCallback callback) {
            this.callback = callback;
        }

        public boolean validate(X509Certificate certificate)
                throws CertificateValidationCallback.CertificateValidationException {
            boolean allUnsupported = true;
            for (int i = 0; i < callbackHandlers.length; i++) {
                CallbackHandler callbackHandler = callbackHandlers[i];
                try {
                    callbackHandler.handle(new Callback[]{callback});
                    allUnsupported = false;
                    if (!callback.getResult()) {
                        return false;
                    }
                }
                catch (IOException e) {
                    throw new CertificateValidationCallback.CertificateValidationException(e);
                }
                catch (UnsupportedCallbackException e) {
                    // ignore
                }
            }
            return !allUnsupported;
        }
    }
}
