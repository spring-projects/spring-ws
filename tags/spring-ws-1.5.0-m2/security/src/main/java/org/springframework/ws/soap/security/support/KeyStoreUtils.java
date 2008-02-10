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

package org.springframework.ws.soap.security.support;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Generic utility methods for dealing with {@link KeyStore} objects.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class KeyStoreUtils {

    /**
     * Loads the key store indicated by system properties. This method tries to load a key store by consulting the
     * following system properties:<code>javax.net.ssl.keyStore</code>, <code>javax.net.ssl.keyStorePassword</code>, and
     * <code>javax.net.ssl.keyStoreType</code>.
     * <p/>
     * If these properties specify a file with an appropriate password, the factory uses this file for the key store. If
     * that file does not exist, then a default, empty keystore is created.
     * <p/>
     * This behavior corresponds to the standard J2SDK behavior for SSL key stores.
     *
     * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html#X509KeyManager">The
     *      standard J2SDK SSL key store mechanism</a>
     */
    public static KeyStore loadDefaultKeyStore() throws GeneralSecurityException, IOException {
        Resource location = null;
        String type = null;
        String password = null;
        String locationProperty = System.getProperty("javax.net.ssl.keyStore");
        if (StringUtils.hasLength(locationProperty)) {
            File f = new File(locationProperty);
            if (f.exists() && f.isFile() && f.canRead()) {
                location = new FileSystemResource(f);
            }
            String passwordProperty = System.getProperty("javax.net.ssl.keyStorePassword");
            if (StringUtils.hasLength(passwordProperty)) {
                password = passwordProperty;
            }
            type = System.getProperty("javax.net.ssl.trustStore");
        }
        // use the factory bean here, easier to setup
        KeyStoreFactoryBean factoryBean = new KeyStoreFactoryBean();
        factoryBean.setLocation(location);
        factoryBean.setPassword(password);
        factoryBean.setType(type);
        factoryBean.afterPropertiesSet();
        return (KeyStore) factoryBean.getObject();
    }

    /**
     * Loads a default trust store. This method uses the following algorithm: <ol> <li> If the system property
     * <code>javax.net.ssl.trustStore</code> is defined, its value is loaded. If the
     * <code>javax.net.ssl.trustStorePassword</code> system property is also defined, its value is used as a password.
     * If the <code>javax.net.ssl.trustStoreType</code> system property is defined, its value is used as a key store
     * type.
     * <p/>
     * If <code>javax.net.ssl.trustStore</code> is defined but the specified file does not exist, then a default, empty
     * trust store is created. </li> <li> If the <code>javax.net.ssl.trustStore</code> system property was not
     * specified, but if the file <code>$JAVA_HOME/lib/security/jssecacerts</code> exists, that file is used. </li>
     * Otherwise, <li>If the file <code>$JAVA_HOME/lib/security/cacerts</code> exists, that file is used. </ol>
     * <p/>
     * This behavior corresponds to the standard J2SDK behavior for SSL trust stores.
     *
     * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager">The
     *      standard J2SDK SSL trust store mechanism</a>
     */
    public static KeyStore loadDefaultTrustStore() throws GeneralSecurityException, IOException {
        Resource location = null;
        String type = null;
        String password = null;
        String locationProperty = System.getProperty("javax.net.ssl.trustStore");
        if (StringUtils.hasLength(locationProperty)) {
            File f = new File(locationProperty);
            if (f.exists() && f.isFile() && f.canRead()) {
                location = new FileSystemResource(f);
            }
            String passwordProperty = System.getProperty("javax.net.ssl.trustStorePassword");
            if (StringUtils.hasLength(passwordProperty)) {
                password = passwordProperty;
            }
            type = System.getProperty("javax.net.ssl.trustStoreType");
        }
        else {
            String javaHome = System.getProperty("java.home");
            location = new FileSystemResource(javaHome + "/lib/security/jssecacerts");
            if (!location.exists()) {
                location = new FileSystemResource(javaHome + "/lib/security/cacerts");
            }
        }
        // use the factory bean here, easier to setup
        KeyStoreFactoryBean factoryBean = new KeyStoreFactoryBean();
        factoryBean.setLocation(location);
        factoryBean.setPassword(password);
        factoryBean.setType(type);
        factoryBean.afterPropertiesSet();
        return (KeyStore) factoryBean.getObject();
    }

}
