/*
 * Copyright ${YEAR} the original author or authors.
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
import org.apache.ws.security.components.crypto.Crypto;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;

public class CryptoFactoryBeanTest extends TestCase {

    private CryptoFactoryBean factoryBean;

    protected void setUp() throws Exception {
        factoryBean = new CryptoFactoryBean();
    }

    public void testMerlin() throws Exception {
        Properties configuration =
                PropertiesLoaderUtils.loadProperties(new ClassPathResource("merlin.properties", getClass()));
        factoryBean.setConfiguration(configuration);
        factoryBean.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
        factoryBean.afterPropertiesSet();

        Object result = factoryBean.getObject();
        assertNotNull("No result", result);
        assertTrue("Not a crypto instance", result instanceof Crypto);
    }
}