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

package org.springframework.ws.soap.security.wss4j.callback;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;

/**
 * A base class for callback handlers.
 *
 * @author Tareq Abed Rabbo
 */
public abstract class AbstractWss4jCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    private boolean passwordDigestRequired;

    private boolean passwordPlainTextRequired;

    private String keyPassword;

    private KeyStore keyStore;

    /** Sets the key store to use if a symmetric key name is embedded. */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /** Sets if a digest password is required. */
    public void setPasswordDigestRequired(boolean passwordDigestRequired) {
        this.passwordDigestRequired = passwordDigestRequired;
    }

    /** Sets the password of the key used for decryption. */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /** Sets if a plain text password is required. */
    public void setPasswordPlainTextRequired(boolean passwordPlainTextRequired) {
        this.passwordPlainTextRequired = passwordPlainTextRequired;
    }

    /** Returns the password of the key used for decryption. */
    public String getKeyPassword() {
        return keyPassword;
    }

    /** Returns if a digest password is required. */
    public boolean isPasswordDigestRequired() {
        return passwordDigestRequired;
    }

    /** Returns if a plain text password is required. */
    public boolean isPasswordPlainTextRequired() {
        return passwordPlainTextRequired;
    }

    /** Gets the key store to use if a symmetric key name is embedded. */
    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void afterPropertiesSet() throws Exception {
        Assert
                .isTrue(!(passwordDigestRequired && passwordPlainTextRequired),
                        "passwordDigestRequired and passwordPlainTextRequired can not be true in the same time");
    }

    protected final void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {

        if (callback instanceof WSPasswordCallback) {
            WSPasswordCallback passwordCallback = (WSPasswordCallback) callback;

            int usage = passwordCallback.getUsage();

            if (passwordDigestRequired && !(usage == WSPasswordCallback.USERNAME_TOKEN)) {
                throw new WSSecurityException("digest password required");
            }

            if (passwordPlainTextRequired && !(usage == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN)) {
                throw new WSSecurityException("plain text password required");
            }

            String id = passwordCallback.getIdentifer();
            switch (usage) {

                // plain text password
                case WSPasswordCallback.USERNAME_TOKEN_UNKNOWN:
                    validateUsernameTokenPlainText(passwordCallback);
                    return;

                    // digest password
                case WSPasswordCallback.USERNAME_TOKEN:
                    validateUsernameTokenDigest(passwordCallback);
                    return;

                    // decryption
                case WSPasswordCallback.DECRYPT:
                    passwordCallback.setPassword(getDecryptionKeyPassword(id));
                    return;

                    // decryption with an embedded symmetric key name
                case WSPasswordCallback.KEY_NAME:
                    try {
                        KeyStore.PasswordProtection protection =
                                new KeyStore.PasswordProtection(getSymmetricKeyPassword(id).toCharArray());
                        Entry entry = keyStore.getEntry(id, protection);
                        if (entry instanceof KeyStore.SecretKeyEntry) {
                            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) entry;
                            SecretKey secretKey = secretKeyEntry.getSecretKey();
                            passwordCallback.setKey(secretKey.getEncoded());
                        }
                        else {
                            throw new RuntimeException("key must be instance of javax.crypto.SecretKey:" + id);
                        }
                    }
                    catch (GeneralSecurityException ex) {
                        throw new Wss4jSecurityCallbackHandlerException(ex
                                .getMessage(), ex);
                    }
                    return;
                default:
                    throw new UnsupportedOperationException("usage type not suporrted:" + usage);
            }
        }
        else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    protected String getDecryptionKeyPassword(String id) {
        return keyPassword;
    }

    protected String getSymmetricKeyPassword(String id) {
        return keyPassword;
    }

    /**
     * validates a Username token with a plain text password. The implementation must validate the username and the
     * password and must throw an exception if the token is not valid
     *
     * @param callback the callback created by Wss4j
     * @throws WSSecurityException if the token is not valid
     */
    abstract protected void validateUsernameTokenPlainText(WSPasswordCallback callback) throws WSSecurityException;

    /**
     * validates a Username token with a digest password. The implementation must fetch the clear password of the and
     * set the password attribute of the callback. Wss4j performs the validation logic.
     *
     * @param callback the callback created by Wss4j
     * @throws WSSecurityException if the token is not valid
     */
    abstract protected void validateUsernameTokenDigest(WSPasswordCallback callback) throws WSSecurityException;
}
