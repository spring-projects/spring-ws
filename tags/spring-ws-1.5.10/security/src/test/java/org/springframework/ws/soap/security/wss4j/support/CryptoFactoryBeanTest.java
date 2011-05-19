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

package org.springframework.ws.soap.security.wss4j.support;

import java.util.Properties;

import junit.framework.TestCase;
import org.apache.ws.security.components.crypto.Merlin;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

public class CryptoFactoryBeanTest extends TestCase {

    private CryptoFactoryBean factoryBean;

    protected void setUp() throws Exception {
        factoryBean = new CryptoFactoryBean();
    }

    public void testSetConfiguration() throws Exception {
        Properties configuration = new Properties();
        configuration.setProperty("org.apache.ws.security.crypto.provider",
                "org.apache.ws.security.components.crypto.Merlin");
        configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "jceks");
        configuration.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "123456");
        configuration.setProperty("org.apache.ws.security.crypto.merlin.file", "private.jks");

        factoryBean.setConfiguration(configuration);
        factoryBean.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
        factoryBean.afterPropertiesSet();

        Object result = factoryBean.getObject();
        assertNotNull("No result", result);
        assertTrue("Not a Merlin instance", result instanceof Merlin);
    }

    public void testProperties() throws Exception {
        factoryBean.setKeyStoreType("jceks");
        factoryBean.setKeyStorePassword("123456");
        factoryBean.setKeyStoreLocation(new ClassPathResource("private.jks"));
        factoryBean.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
        factoryBean.afterPropertiesSet();
        Object result = factoryBean.getObject();
        assertNotNull("No result", result);
        assertTrue("Not a Merlin instance", result instanceof Merlin);
    }
}