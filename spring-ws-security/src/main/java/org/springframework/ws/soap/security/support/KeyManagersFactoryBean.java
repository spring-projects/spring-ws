/*
 * Copyright 2005-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.security.support;

import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Spring factory bean for an array of {@link KeyManager}s.
 * <p/>
 * Uses the {@link KeyManagerFactory} to create the {@code KeyManager}s.
 *
 * @author Stephen More
 * @author Arjen Poutsma
 * @see KeyManager
 * @see KeyManagerFactory
 * @since 2.1.2
 */
public class KeyManagersFactoryBean implements FactoryBean<KeyManager[]>, InitializingBean {

    private KeyManagerFactory keyManagerFactory;

    private KeyStore keyStore;

    private String algorithm;

    private String provider;

    private char[] password;

    /**
     * Sets the password to use for integrity checking. If this property is not set, then integrity checking is not
     * performed.
     */
    public void setPassword(String password) {
        if (password != null) {
            this.password = password.toCharArray();
        }
    }

    /**
     * Sets the provider of the key store to use. If this is not set, the default is used.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Sets the algorithm of the <code>KeyManager</code> to use. If this is not set, the default is used.
     *
     * @see KeyManagerFactory#getDefaultAlgorithm()
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Sets the source of key material.
     *
     * @see KeyManagerFactory#init(KeyStore, char[])
     */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public KeyManager[] getObject() throws Exception {
        return keyManagerFactory.getKeyManagers();
    }

    public Class<?> getObjectType() {
        return KeyManager[].class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        String algorithm =
                StringUtils.hasLength(this.algorithm) ? this.algorithm : KeyManagerFactory.getDefaultAlgorithm();

        keyManagerFactory =
                StringUtils.hasLength(this.provider) ? KeyManagerFactory.getInstance(algorithm, this.provider) :
                        KeyManagerFactory.getInstance(algorithm);

        keyManagerFactory.init(keyStore, password);
    }
}
