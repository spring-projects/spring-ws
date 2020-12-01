/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http.support;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

/**
 * Base class for {@link WebApplicationInitializer} implementations that register a {@link MessageDispatcherServlet}
 * configured with annotated classes, e.g. Spring's {@link Configuration @Configuration} classes.
 * <p>
 * Concrete implementations are required to implement {@link #getRootConfigClasses()} and
 * {@link #getServletConfigClasses()} as well as {@link #getServletMappings()}. Further template and customization
 * methods are provided by {@link AbstractDispatcherServletInitializer}.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public abstract class AbstractAnnotationConfigMessageDispatcherServletInitializer
		extends AbstractMessageDispatcherServletInitializer {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation creates an {@link AnnotationConfigWebApplicationContext}, providing it the annotated classes
	 * returned by {@link #getRootConfigClasses()}. Returns {@code null} if {@link #getRootConfigClasses()} returns
	 * {@code null}.
	 */
	@Override
	protected WebApplicationContext createRootApplicationContext() {
		Class<?>[] configClasses = getRootConfigClasses();
		if (!ObjectUtils.isEmpty(configClasses)) {
			AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
			rootAppContext.register(configClasses);
			return rootAppContext;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation creates an {@link AnnotationConfigWebApplicationContext}, providing it the annotated classes
	 * returned by {@link #getServletConfigClasses()}.
	 */
	@Override
	protected WebApplicationContext createServletApplicationContext() {
		AnnotationConfigWebApplicationContext servletAppContext = new AnnotationConfigWebApplicationContext();
		Class<?>[] configClasses = getServletConfigClasses();
		if (!ObjectUtils.isEmpty(configClasses)) {
			servletAppContext.register(configClasses);
		}
		return servletAppContext;
	}

	/**
	 * Specify {@link org.springframework.context.annotation.Configuration @Configuration} and/or
	 * {@link org.springframework.stereotype.Component @Component} classes to be provided to the
	 * {@linkplain #createRootApplicationContext() root application context}.
	 * 
	 * @return the configuration classes for the root application context, or {@code null} if creation and registration of
	 *         a root context is not desired
	 */
	protected abstract Class<?>[] getRootConfigClasses();

	/**
	 * Specify {@link org.springframework.context.annotation.Configuration @Configuration} and/or
	 * {@link org.springframework.stereotype.Component @Component} classes to be provided to the
	 * {@linkplain #createServletApplicationContext() dispatcher servlet application context}.
	 * 
	 * @return the configuration classes for the dispatcher servlet application context (may not be empty or {@code null})
	 */
	protected abstract Class<?>[] getServletConfigClasses();

}
