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

package org.springframework.oxm.jibx;

import junit.framework.TestCase;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;

public class JibxBindingFactoryBeanTest extends TestCase {

    private JibxBindingFactoryBean factoryBean;

    protected void setUp() throws Exception {
        factoryBean = new JibxBindingFactoryBean();
    }

    public void testAfterPropertiesSet() throws Exception {
        try {
            factoryBean.setTargetClass(getClass());
            factoryBean.afterPropertiesSet();
            fail("Should have thrown an JibxException");
        }
        catch (JiBXException ex) {
        }
    }

    public void testAfterPropertiesSetTargetClass() throws Exception {
        try {
            factoryBean.afterPropertiesSet();
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void testGetObjectType() throws Exception {
        assertTrue("getObjectType does not return JAXBContext",
                factoryBean.getObjectType().equals(IBindingFactory.class));
    }

    public void testIsSingleton() throws Exception {
        assertTrue("not a singleton", factoryBean.isSingleton());
    }
}