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

package org.springframework.ws.test.support;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Helper class for for loading default implementations of an interface.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class MockStrategiesHelper {

	private static final Log logger = LogFactory.getLog(MockStrategiesHelper.class);

	private final ApplicationContext applicationContext;

	/**
	 * Creates a new instance of the {@code MockStrategiesHelper} with the given application context.
	 *
	 * @param applicationContext the application context
	 */
	public MockStrategiesHelper(ApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "'applicationContext' must not be null");
		this.applicationContext = applicationContext;
	}

	/**
	 * Returns the application context.
	 */
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Returns a single strategy found in the given application context.
	 *
	 * @param type the type of bean to be found in the application context
	 * @return the bean, or {@code null} if no bean of the given type can be found
	 * @throws BeanInitializationException if there is more than 1 beans of the given type
	 */
	public <T> T getStrategy(Class<T> type) {
		Assert.notNull(type, "'type' must not be null");
		Map<String, T> map = applicationContext.getBeansOfType(type);
		if (map.isEmpty()) {
			return null;
		} else if (map.size() == 1) {
			Map.Entry<String, T> entry = map.entrySet().iterator().next();
			if (logger.isDebugEnabled()) {
				logger.debug("Using " + ClassUtils.getShortName(type) + " [" + entry.getKey() + "]");
			}
			return entry.getValue();
		} else {
			throw new BeanInitializationException(
					"Could not find exactly 1 " + ClassUtils.getShortName(type) + " in application context");
		}
	}

	/**
	 * Returns a single strategy found in the given application context, or instantiates a default strategy if no
	 * applicable strategy was found.
	 *
	 * @param type the type of bean to be found in the application context
	 * @param defaultType the type to instantiate and return when no bean of the specified type could be found
	 * @return the bean found in the application context, or the default type if no bean of the given type can be found
	 * @throws BeanInitializationException if there is more than 1 beans of the given type
	 */
	public <T, D extends T> T getStrategy(Class<T> type, Class<D> defaultType) {
		Assert.notNull(defaultType, "'defaultType' must not be null");
		T t = getStrategy(type);
		if (t != null) {
			return t;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"No " + ClassUtils.getShortName(type) + " found, using default " + ClassUtils.getShortName(defaultType));
			}
			T defaultStrategy = BeanUtils.instantiateClass(defaultType);
			if (defaultStrategy instanceof ApplicationContextAware) {
				ApplicationContextAware applicationContextAware = (ApplicationContextAware) defaultStrategy;
				applicationContextAware.setApplicationContext(applicationContext);
			}
			if (defaultStrategy instanceof InitializingBean) {
				InitializingBean initializingBean = (InitializingBean) defaultStrategy;
				try {
					initializingBean.afterPropertiesSet();
				} catch (Exception ex) {
					throw new BeanCreationException("Invocation of init method failed", ex);
				}
			}
			return defaultStrategy;
		}
	}

}
