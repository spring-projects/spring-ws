/*
 * Copyright 2005-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http.support;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

import org.springframework.util.Assert;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

/**
 * Base class for {@link org.springframework.web.WebApplicationInitializer WebApplicationInitializer} implementations
 * that register a {@link MessageDispatcherServlet} in the servlet context.
 * <p>
 * Concrete implementations are required to implement {@link #createServletApplicationContext()}, which gets invoked
 * from {@link #registerMessageDispatcherServlet(ServletContext)}. Further customization can be achieved by overriding
 * {@link #customizeRegistration(ServletRegistration.Dynamic)}.
 * <p>
 * Because this class extends from {@link AbstractContextLoaderInitializer}, concrete implementations are also required
 * to implement {@link #createRootApplicationContext()} to set up a parent "<strong>root</strong>" application context.
 * If a root context is not desired, implementations can simply return {@code null} in the
 * {@code createRootApplicationContext()} implementation.
 *
 * @author Arjen Poutsma
 * @since 2.2
 */
public abstract class AbstractMessageDispatcherServletInitializer extends AbstractContextLoaderInitializer {

	/**
	 * The default servlet name. Can be customized by overriding {@link #getServletName}.
	 */
	public static final String DEFAULT_SERVLET_NAME = "messageDispatcher";

	/**
	 * The default servlet mappings. Can be customized by overriding {@link #getServletMappings()}.
	 */
	public static final String[] DEFAULT_SERVLET_MAPPINGS = new String[] { "/services", "*.wsdl" };

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);

		this.registerMessageDispatcherServlet(servletContext);
	}

	/**
	 * Register a {@link MessageDispatcherServlet} against the given servlet context.
	 * <p>
	 * This method will create a {@code MessageDispatcherServlet} with the name returned by {@link #getServletName()},
	 * initializing it with the application context returned from {@link #createServletApplicationContext()}, and mapping
	 * it to the patterns returned from {@link #getServletMappings()}.
	 * <p>
	 * Further customization can be achieved by overriding {@link #customizeRegistration(ServletRegistration.Dynamic)}.
	 *
	 * @param servletContext the context to register the servlet against
	 */
	protected void registerMessageDispatcherServlet(ServletContext servletContext) {
		String servletName = this.getServletName();
		Assert.hasLength(servletName, "getServletName() may not return empty or null");

		WebApplicationContext servletAppContext = this.createServletApplicationContext();
		Assert.notNull(servletAppContext, "createServletApplicationContext() did not return an application "
				+ "context for servlet [" + servletName + "]");

		MessageDispatcherServlet dispatcherServlet = new MessageDispatcherServlet(servletAppContext);
		dispatcherServlet.setTransformWsdlLocations(isTransformWsdlLocations());
		dispatcherServlet.setTransformSchemaLocations(isTransformSchemaLocations());

		ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, dispatcherServlet);

		Assert.notNull(registration, "Failed to register servlet with name '" + servletName + "'."
				+ "Check if there is another servlet registered under the same name.");

		registration.setLoadOnStartup(1);
		registration.addMapping(getServletMappings());

		this.customizeRegistration(registration);
	}

	/**
	 * Return the name under which the {@link MessageDispatcherServlet} will be registered. Defaults to
	 * {@link #DEFAULT_SERVLET_NAME}.
	 *
	 * @see #registerMessageDispatcherServlet(ServletContext)
	 */
	protected String getServletName() {
		return DEFAULT_SERVLET_NAME;
	}

	/**
	 * Create a servlet application context to be provided to the {@code MessageDispatcherServlet}.
	 * <p>
	 * The returned context is delegated to Spring's
	 * {@link MessageDispatcherServlet#MessageDispatcherServlet(WebApplicationContext)}. As such, it typically contains
	 * endpoints, interceptors and other web service-related beans.
	 *
	 * @see #registerMessageDispatcherServlet(ServletContext)
	 */
	protected abstract WebApplicationContext createServletApplicationContext();

	/**
	 * Specify the servlet mapping(s) for the {@code MessageDispatcherServlet}. Defaults to
	 * {@link #DEFAULT_SERVLET_MAPPINGS}.
	 *
	 * @see #registerMessageDispatcherServlet(ServletContext)
	 */
	protected String[] getServletMappings() {
		return DEFAULT_SERVLET_MAPPINGS;
	}

	/**
	 * Indicates whether relative address locations in the WSDL are to be transformed using the request URI of the
	 * incoming HTTP request. Defaults to {@code false}.
	 */
	public boolean isTransformWsdlLocations() {
		return false;
	}

	/**
	 * Indicates whether relative address locations in the XSD are to be transformed using the request URI of the incoming
	 * HTTP request. Defaults to {@code false}.
	 */
	protected boolean isTransformSchemaLocations() {
		return false;
	}

	/**
	 * Optionally perform further registration customization once
	 * {@link #registerMessageDispatcherServlet(ServletContext)} has completed.
	 *
	 * @param registration the {@code MessageDispatcherServlet} registration to be customized
	 * @see #registerMessageDispatcherServlet(ServletContext)
	 */
	protected void customizeRegistration(ServletRegistration.Dynamic registration) {}

}
