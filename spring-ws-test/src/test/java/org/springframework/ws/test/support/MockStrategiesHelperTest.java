/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.test.support;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.support.StaticApplicationContext;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MockStrategiesHelperTest {

	@Test
	public void none() {
		StaticApplicationContext applicationContext = new StaticApplicationContext();

		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
		assertNull(helper.getStrategy(IMyBean.class));
	}

	@Test
	public void one() {
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("myBean", MyBean.class);

		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
		assertNotNull(helper.getStrategy(IMyBean.class));
	}

	@Test(expected = BeanInitializationException.class)
	public void many() {
		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("myBean1", MyBean.class);
		applicationContext.registerSingleton("myBean2", MyBean.class);

		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
		helper.getStrategy(IMyBean.class);
	}
	
	@Test
	public void noneWithDefault() {
		StaticApplicationContext applicationContext = new StaticApplicationContext();


		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
		assertNotNull(helper.getStrategy(IMyBean.class, MyBean.class));
	}


	public interface IMyBean {

	}

	public static class MyBean implements IMyBean {

	}

}
