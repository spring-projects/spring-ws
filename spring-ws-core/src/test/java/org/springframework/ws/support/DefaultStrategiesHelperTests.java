/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.support;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DefaultStrategiesHelperTests {

	@Test
	void testGetDefaultStrategies() {

		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(),
				StrategyImpl.class.getName() + "," + ContextAwareStrategyImpl.class.getName());
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("strategy1", StrategyImpl.class);
		applicationContext.registerSingleton("strategy2", ContextAwareStrategyImpl.class);

		List<Strategy> result = helper.getDefaultStrategies(Strategy.class, applicationContext);

		assertThat(result).isNotNull();
		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isNotNull();
		assertThat(result.get(1)).isNotNull();
		assertThat(result.get(0)).isInstanceOf(StrategyImpl.class);
		assertThat(result.get(1)).isInstanceOf(ContextAwareStrategyImpl.class);

		ContextAwareStrategyImpl impl = (ContextAwareStrategyImpl) result.get(1);

		assertThat(impl.getApplicationContext()).isNotNull();
	}

	@Test
	void testGetDefaultStrategy() {

		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(), StrategyImpl.class.getName());
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("strategy1", StrategyImpl.class);
		applicationContext.registerSingleton("strategy2", ContextAwareStrategyImpl.class);

		Object result = helper.getDefaultStrategy(Strategy.class, applicationContext);

		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Strategy.class);
		assertThat(result).isInstanceOf(StrategyImpl.class);
	}

	@Test
	void testGetDefaultStrategyMoreThanOne() {

		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(),
				StrategyImpl.class.getName() + "," + ContextAwareStrategyImpl.class.getName());
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

		StaticApplicationContext applicationContext = new StaticApplicationContext();
		applicationContext.registerSingleton("strategy1", StrategyImpl.class);
		applicationContext.registerSingleton("strategy2", ContextAwareStrategyImpl.class);

		assertThatExceptionOfType(BeanInitializationException.class)
			.isThrownBy(() -> helper.getDefaultStrategy(Strategy.class, applicationContext));
	}

	@Test
	void testResourceConstructor() {

		Resource resource = new ClassPathResource("strategies.properties", getClass());
		new DefaultStrategiesHelper(resource);
	}

	public interface Strategy {

	}

	private static final class StrategyImpl implements Strategy {

	}

	private static final class ContextAwareStrategyImpl implements Strategy, ApplicationContextAware {

		private ApplicationContext applicationContext;

		public ApplicationContext getApplicationContext() {
			return this.applicationContext;
		}

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

	}

}
