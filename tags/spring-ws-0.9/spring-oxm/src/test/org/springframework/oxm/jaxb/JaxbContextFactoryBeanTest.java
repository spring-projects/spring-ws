/*
 * Copyright 2005 the original author or authors.
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
package org.springframework.oxm.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

public class JaxbContextFactoryBeanTest extends TestCase {

    private JaxbContextFactoryBean jaxbContextFactoryBean;

    protected void setUp() throws Exception {
        jaxbContextFactoryBean = new JaxbContextFactoryBean();
    }

    public void testGetObjectType() throws Exception {
        assertTrue("getObjectType does not return JAXBContext",
                jaxbContextFactoryBean.getObjectType().equals(JAXBContext.class));
    }

    public void testIsSingleton() throws Exception {
        assertTrue("not a singleton", jaxbContextFactoryBean.isSingleton());
    }

    public void testAfterPropertiesSetNoContextPath() throws Exception {
        try {
            jaxbContextFactoryBean.afterPropertiesSet();
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
        }
    }

    public void testAfterPropertiesSet() throws Exception {
        try {
            jaxbContextFactoryBean.setContextPath("ab");
            jaxbContextFactoryBean.afterPropertiesSet();
            fail("Should have thrown an JAXBException");
        }
        catch (JAXBException ex) {
        }
    }
}