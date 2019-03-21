/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xml.xpath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XPathExpressionFactoryBeanTest {

	private XPathExpressionFactoryBean factoryBean;

	@Before
	public void setUp() throws Exception {
		factoryBean = new XPathExpressionFactoryBean();
	}

	@Test
	public void testFactoryBean() throws Exception {
		factoryBean.setExpression("/root");
		factoryBean.afterPropertiesSet();
		Object result = factoryBean.getObject();
		Assert.assertNotNull("No result obtained", result);
		Assert.assertTrue("No XPathExpression returned", result instanceof XPathExpression);
		Assert.assertTrue("Not a singleton", factoryBean.isSingleton());
		Assert.assertEquals("Not a XPathExpresison", XPathExpression.class, factoryBean.getObjectType());
	}
}