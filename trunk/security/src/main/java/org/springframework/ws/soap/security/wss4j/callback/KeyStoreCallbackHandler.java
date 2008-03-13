/*
 * Copyright 2008 the original author or authors.
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
import java.security.Key;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ws.soap.security.support.KeyStoreUtils;

/**
 * Callback handler that uses Java Security <code>KeyStore</code>s to handle cryptographic callbacks. Allows for
 * specific key stores to be set for various cryptographic operations.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.security.support.KeyStoreFactoryBean
 * @since 1.5.0
 */
public class KeyStoreCallbackHandler extends AbstractWsPasswordCallbackHandler implements InitializingBean {

    private String privateKeyPassword;

    private char[] symmetricKeyPassword;

    private KeyStore keyStore;

    /** Sets the key store to use if a symmetric key name is embedded. */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Sets the password used to retrieve private keys from the keystore. This property is required for decryption based
     * on private keys, and signing.
     */
    public void setPrivateKeyPassword(String privateKeyPassword) {
        if (privateKeyPassword != null) {
            this.privateKeyPassword = privateKeyPassword;
        }
    }

    /**
     * Sets the password used to retrieve keys from the symmetric keystore. If this property is not set, it defaults to
     * the private key password.
     *
     * @see #setPrivateKeyPassword(String)
     */
    public void setSymmetricKeyPassword(String symmetricKeyPassword) {
        if (symmetricKeyPassword != null) {
            this.symmetricKeyPassword = symmetricKeyPassword.toCharArray();
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (keyStore == null) {
            loadDefaultKeyStore();
        }
        if (symmetricKeyPassword == null) {
            symmetricKeyPassword = privateKeyPassword.toCharArray();
        }
    }

    protected void handleDecrypt(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
        callback.setPassword(privateKeyPassword);
    }

    protected void handleKeyName(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
        try {
            String identifier = callback.getIdentifer();
            Key key = keyStore.getKey(identifier, symmetricKeyPassword);
            if (key instanceof SecretKey) {
                callback.setKey(key.getEncoded());
            }
            else {
                throw new WSSecurityException("Key [" + key + "] is not a javax.crypto.SecretKey");
            }
        }
        catch (GeneralSecurityException ex) {
            throw new WSSecurityException("Could not obtain symmetric key", ex);
        }
    }

    /** Loads the key store indicated by system properties. Delegates to {@link KeyStoreUtils#loadDefaultKeyStore()}. */
    protected void loadDefaultKeyStore() {
        try {
            keyStore = KeyStoreUtils.loadDefaultKeyStore();
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded default key store");
            }
        }
        catch (Exception ex) {
            logger.warn("Could not open default key store", ex);
        }
    }

}
