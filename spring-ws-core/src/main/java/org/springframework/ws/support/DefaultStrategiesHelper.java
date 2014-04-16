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

package org.springframework.ws.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletContext;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * Helper class for for loading default implementations of an interface. Encapsulates a properties object, which
 * contains strategy interface names as keys, and comma-separated class names as values.
 *
 * <p>Simulates the {@link BeanFactory normal lifecycle} for beans, by calling {@link
 * BeanFactoryAware#setBeanFactory(BeanFactory)}, {@link ApplicationContextAware#setApplicationContext(ApplicationContext)},
 * etc.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class DefaultStrategiesHelper {

    /** Keys are strategy interface names, values are implementation class names. */
    private Properties defaultStrategies;

    /** Initializes a new instance of the {@code DefaultStrategiesHelper} based on the given set of properties. */
    public DefaultStrategiesHelper(Properties defaultStrategies) {
        Assert.notNull(defaultStrategies, "defaultStrategies must not be null");
        this.defaultStrategies = defaultStrategies;
    }

    /** Initializes a new instance of the {@code DefaultStrategiesHelper} based on the given resource. */
    public DefaultStrategiesHelper(Resource resource) throws IllegalStateException {
        try {
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Could not load '" + resource + "': " + ex.getMessage());
        }
    }

    /**
     * Initializes a new instance of the {@code DefaultStrategiesHelper} based on the given type.
     *
     * <p>This constructor will attempt to load a 'typeName'.properties file in the same package as the given type.
     */
    public DefaultStrategiesHelper(Class<?> type) {
        this(new ClassPathResource(ClassUtils.getShortName(type) + ".properties", type));
    }

    /**
     * Create a list of strategy objects for the given strategy interface. Strategies are retrieved from the
     * {@code Properties} object given at construction-time.
     *
     * @param strategyInterface the strategy interface
     * @return a list of corresponding strategy objects
     * @throws BeansException if initialization failed
     */
    public <T> List<T> getDefaultStrategies(Class<T> strategyInterface) throws BeanInitializationException {
        return getDefaultStrategies(strategyInterface, null);
    }

    /**
     * Create a list of strategy objects for the given strategy interface. Strategies are retrieved from the
     * {@code Properties} object given at construction-time. It instantiates the strategy objects and satisfies
     * {@code ApplicationContextAware} with the supplied context if necessary.
     *
     * @param strategyInterface  the strategy interface
     * @param applicationContext used to satisfy strategies that are application context aware, may be
     *                           {@code null}
     * @return a list of corresponding strategy objects
     * @throws BeansException if initialization failed
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getDefaultStrategies(Class<T> strategyInterface, ApplicationContext applicationContext)
            throws BeanInitializationException {
        String key = strategyInterface.getName();
        try {
            List<T> result;
            String value = defaultStrategies.getProperty(key);
            if (value != null) {
                String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
                result = new ArrayList<T>(classNames.length);
                ClassLoader classLoader = null;
                if (applicationContext != null) {
                    classLoader = applicationContext.getClassLoader();
                }
                if (classLoader == null) {
                    classLoader = DefaultStrategiesHelper.class.getClassLoader();
                }
                for (String className : classNames) {
                    Class<T> clazz = (Class<T>) ClassUtils.forName(className, classLoader);
                    Assert.isTrue(strategyInterface.isAssignableFrom(clazz), clazz.getName() + " is not a " + strategyInterface.getName());
                    T strategy = instantiateBean(clazz, applicationContext);
                    result.add(strategy);
                }
            }
            else {
                result = Collections.emptyList();
            }
            Collections.sort(result, new OrderComparator());
            return result;
        }
        catch (ClassNotFoundException ex) {
            throw new BeanInitializationException("Could not find default strategy class for interface [" + key + "]",
                    ex);
        }
    }

    /** Instantiates the given bean, simulating the standard bean life cycle. */
    private <T> T instantiateBean(Class<T> clazz, ApplicationContext applicationContext) {
        T strategy = BeanUtils.instantiateClass(clazz);
        if (strategy instanceof BeanNameAware) {
            BeanNameAware beanNameAware = (BeanNameAware) strategy;
            beanNameAware.setBeanName(clazz.getName());
        }
        if (applicationContext != null) {
            if (strategy instanceof BeanClassLoaderAware) {
                ((BeanClassLoaderAware) strategy).setBeanClassLoader(applicationContext.getClassLoader());
            }
            if (strategy instanceof BeanFactoryAware) {
                ((BeanFactoryAware) strategy).setBeanFactory(applicationContext);
            }
            if (strategy instanceof ResourceLoaderAware) {
                ((ResourceLoaderAware) strategy).setResourceLoader(applicationContext);
            }
            if (strategy instanceof ApplicationEventPublisherAware) {
                ((ApplicationEventPublisherAware) strategy).setApplicationEventPublisher(applicationContext);
            }
            if (strategy instanceof MessageSourceAware) {
                ((MessageSourceAware) strategy).setMessageSource(applicationContext);
            }
            if (strategy instanceof ApplicationContextAware) {
                ApplicationContextAware applicationContextAware = (ApplicationContextAware) strategy;
                applicationContextAware.setApplicationContext(applicationContext);
            }
            if (applicationContext instanceof WebApplicationContext && strategy instanceof ServletContextAware) {
                ServletContext servletContext = ((WebApplicationContext) applicationContext).getServletContext();
                ((ServletContextAware) strategy).setServletContext(servletContext);
            }
        }
        if (strategy instanceof InitializingBean) {
            InitializingBean initializingBean = (InitializingBean) strategy;
            try {
                initializingBean.afterPropertiesSet();
            }
            catch (Throwable ex) {
                throw new BeanCreationException("Invocation of init method failed", ex);
            }
        }
        return strategy;
    }

    /**
     * Return the default strategy object for the given strategy interface.
     *
     * @param strategyInterface the strategy interface
     * @return the corresponding strategy object
     * @throws BeansException if initialization failed
     * @see #getDefaultStrategies
     */
    public <T> T getDefaultStrategy(Class<T> strategyInterface) throws BeanInitializationException {
        return getDefaultStrategy(strategyInterface, null);
    }

    /**
     * Return the default strategy object for the given strategy interface.
     *
     * <p>Delegates to {@link #getDefaultStrategies(Class,ApplicationContext)}, expecting a single object in the list.
     *
     * @param strategyInterface  the strategy interface
     * @param applicationContext used to satisfy strategies that are application context aware, may be
     *                           {@code null}
     * @return the corresponding strategy object
     * @throws BeansException if initialization failed
     */
    public <T> T getDefaultStrategy(Class<T> strategyInterface, ApplicationContext applicationContext)
            throws BeanInitializationException {
        List<T> result = getDefaultStrategies(strategyInterface, applicationContext);
        if (result.size() != 1) {
            throw new BeanInitializationException(
                    "Could not find exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
        }
        return result.get(0);
    }

}
