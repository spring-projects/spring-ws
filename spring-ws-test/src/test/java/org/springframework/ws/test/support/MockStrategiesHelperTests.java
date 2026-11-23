/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.support.StaticApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MockStrategiesHelperTests {

	@Test
	void none() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();

		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);

		assertThat(helper.getStrategy(IMyBean.class)).isNull();
	}

	@Test
	void one() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("myBean", MyBean.class);

		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
		assertThat(helper.getStrategy(IMyBean.class)).isNotNull();
	}

	@Test
	void many() {

		assertThatExceptionOfType(BeanInitializationException.class).isThrownBy(() -> {

			StaticApplicationContext applicationContext = new StaticApplicationContext();
			applicationContext.registerSingleton("myBean1", MyBean.class);
			applicationContext.registerSingleton("myBean2", MyBean.class);

			MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
			helper.getStrategy(IMyBean.class);
		});
	}

	@Test
	void noneWithDefault() {

		StaticApplicationContext applicationContext = new StaticApplicationContext();

		MockStrategiesHelper helper = new MockStrategiesHelper(applicationContext);
		assertThat(helper.getStrategy(IMyBean.class, MyBean.class)).isNotNull();
	}

	public interface IMyBean {

	}

	public static class MyBean implements IMyBean {

	}

}
