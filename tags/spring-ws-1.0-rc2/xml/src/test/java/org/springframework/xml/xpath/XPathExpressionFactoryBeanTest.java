/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.xml.xpath;

import junit.framework.TestCase;

public class XPathExpressionFactoryBeanTest extends TestCase {

    private XPathExpressionFactoryBean factoryBean;

    protected void setUp() throws Exception {
        factoryBean = new XPathExpressionFactoryBean();
    }

    public void testFactoryBean() throws Exception {
        factoryBean.setExpression("/root");
        factoryBean.afterPropertiesSet();
        Object result = factoryBean.getObject();
        assertNotNull("No result obtained", result);
        assertTrue("No XPathExpression returned", result instanceof XPathExpression);
        assertTrue("Not a singleton", factoryBean.isSingleton());
        assertEquals("Not a XPathExpresison", XPathExpression.class, factoryBean.getObjectType());
    }
}