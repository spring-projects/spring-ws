/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.support;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link DefaultStrategiesHelper}.
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 */
class DefaultStrategiesHelperTests {

	@Test
	void getDefaultStrategies() {
		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(),
				Stream.of(StrategyImpl.class, ContextAwareStrategyImpl.class)
					.map(Class::getName)
					.collect(Collectors.joining(",")));
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.refresh();

		List<Strategy> result = helper.getDefaultStrategies(Strategy.class, applicationContext);

		assertThat(result).hasSize(2);
		assertThat(result).element(0).isInstanceOf(StrategyImpl.class);
		assertThat(result).element(1).isInstanceOfSatisfying(ContextAwareStrategyImpl.class, (impl) -> {
			assertThat(impl.applicationContext).isSameAs(applicationContext);
			assertThat(impl.resourceLoader).isSameAs(applicationContext);
			assertThat(impl.initialized).isTrue();
		});
	}

	@Test
	void getDefaultStrategy() {
		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(), ContextAwareStrategyImpl.class.getName());
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.refresh();

		Object result = helper.getDefaultStrategy(Strategy.class, applicationContext);
		assertThat(result).isInstanceOfSatisfying(ContextAwareStrategyImpl.class, (impl) -> {
			assertThat(impl.applicationContext).isSameAs(applicationContext);
			assertThat(impl.resourceLoader).isSameAs(applicationContext);
			assertThat(impl.initialized).isTrue();
		});
	}

	@Test
	void getDefaultStrategyWithoutApplicationContext() {
		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(), ContextAwareStrategyImpl.class.getName());
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

		Object result = helper.getDefaultStrategy(Strategy.class, null);
		assertThat(result).isInstanceOfSatisfying(ContextAwareStrategyImpl.class, (impl) -> {
			assertThat(impl.applicationContext).isNull();
			assertThat(impl.resourceLoader).isNull();
			assertThat(impl.initialized).isTrue();
		});
	}

	@Test
	void getDefaultStrategyWithMultipleCandidates() {
		Properties strategies = new Properties();
		strategies.put(Strategy.class.getName(),
				StrategyImpl.class.getName() + "," + ContextAwareStrategyImpl.class.getName());
		DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

		assertThatExceptionOfType(BeanInitializationException.class)
			.isThrownBy(() -> helper.getDefaultStrategy(Strategy.class, null));
	}

	@Test
	void createWithResource() {
		Resource resource = new ClassPathResource("strategies.properties", getClass());
		new DefaultStrategiesHelper(resource);
	}

	public interface Strategy {

	}

	private static final class StrategyImpl implements Strategy {

	}

	private static final class ContextAwareStrategyImpl
			implements Strategy, ApplicationContextAware, ResourceLoaderAware, InitializingBean {

		private ApplicationContext applicationContext;

		private ResourceLoader resourceLoader;

		private boolean initialized;

		@Override
		public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
			this.applicationContext = applicationContext;
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			this.initialized = true;
		}

	}

}
