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

package org.springframework.ws.soap.security.wss4j.support;

import java.util.Properties;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Spring factory bean for a WSS4J {@link Crypto}.
 * <p/>
 * Requires the {@link #setConfiguration(java.util.Properties) configuration} property to be set. This configuration
 * should have the <code>org.apache.ws.security.crypto.provider</code> property defined.
 *
 * @author Tareq Abed Rabbo
 * @author Arjen Poutsma
 * @see org.apache.ws.security.components.crypto.Crypto
 * @since 1.5.0
 */
public class CryptoFactoryBean implements FactoryBean, BeanClassLoaderAware, InitializingBean {

    private Properties configuration;

    private ClassLoader classLoader;

    private Crypto crypto;

    /**
     * Sets the configuration of the Crypto.
     *
     * @see org.apache.ws.security.components.crypto.CryptoFactory#getInstance(java.util.Properties)
     */
    public void setConfiguration(Properties properties) {
        this.configuration = properties;
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(configuration, "'configuration' is required");

        this.crypto = CryptoFactory.getInstance(configuration, classLoader);
    }

    public Class getObjectType() {
        return Crypto.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getObject() throws Exception {
        return crypto;
    }

}
