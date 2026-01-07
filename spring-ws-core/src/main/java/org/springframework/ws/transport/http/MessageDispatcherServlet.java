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

package org.springframework.ws.transport.http;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.support.DefaultStrategiesHelper;
import org.springframework.ws.support.WebUtils;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Servlet for simplified dispatching of Web service messages.
 * <p>
 * This servlet is a convenient alternative to the standard Spring-MVC
 * {@link DispatcherServlet} with separate
 * {@link WebServiceMessageReceiverHandlerAdapter}, {@link MessageDispatcher}, and
 * {@link WsdlDefinitionHandlerAdapter} instances.
 * <p>
 * This servlet automatically detects {@link EndpointAdapter EndpointAdapters},
 * {@link EndpointMapping EndpointMappings}, and {@link EndpointExceptionResolver
 * EndpointExceptionResolvers} <i>by type</i>.
 * <p>
 * This servlet also automatically detects any {@link WsdlDefinition} defined in its
 * application context. This WSDL is exposed under the bean name: for example, a
 * {@code WsdlDefinition} bean named '{@code echo}' will be exposed as {@code echo.wsdl}
 * in this servlet's context: {@code http://localhost:8080/spring-ws/echo.wsdl}. When the
 * {@code transformWsdlLocations} init-param is set to {@code true} in this servlet's
 * configuration in {@code web.xml}, all {@code location} attributes in the WSDL
 * definitions will reflect the URL of the incoming request.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.ws.server.MessageDispatcher
 * @see org.springframework.ws.transport.http.WebServiceMessageReceiverHandlerAdapter
 */
@SuppressWarnings("serial")
public class MessageDispatcherServlet extends FrameworkServlet {

	/**
	 * Well-known name for the {@link WebServiceMessageFactory} bean in the bean factory
	 * for this namespace.
	 */
	public static final String DEFAULT_MESSAGE_FACTORY_BEAN_NAME = "messageFactory";

	/**
	 * Well-known name for the {@link WebServiceMessageReceiver} object in the bean
	 * factory for this namespace.
	 */
	public static final String DEFAULT_MESSAGE_RECEIVER_BEAN_NAME = "messageReceiver";

	/**
	 * Well-known name for the {@link WebServiceMessageReceiverHandlerAdapter} object in
	 * the bean factory for this namespace.
	 */
	public static final String DEFAULT_MESSAGE_RECEIVER_HANDLER_ADAPTER_BEAN_NAME = "messageReceiverHandlerAdapter";

	/**
	 * Well-known name for the {@link WsdlDefinitionHandlerAdapter} object in the bean
	 * factory for this namespace.
	 */
	public static final String DEFAULT_WSDL_DEFINITION_HANDLER_ADAPTER_BEAN_NAME = "wsdlDefinitionHandlerAdapter";

	/**
	 * Well-known name for the {@link XsdSchemaHandlerAdapter} object in the bean factory
	 * for this namespace.
	 */
	public static final String DEFAULT_XSD_SCHEMA_HANDLER_ADAPTER_BEAN_NAME = "xsdSchemaHandlerAdapter";

	/** Suffix of a WSDL request uri. */
	private static final String WSDL_SUFFIX_NAME = ".wsdl";

	/** Suffix of a XSD request uri. */
	private static final String XSD_SUFFIX_NAME = ".xsd";

	private final DefaultStrategiesHelper defaultStrategiesHelper;

	private String messageFactoryBeanName = DEFAULT_MESSAGE_FACTORY_BEAN_NAME;

	private String messageReceiverHandlerAdapterBeanName = DEFAULT_MESSAGE_RECEIVER_HANDLER_ADAPTER_BEAN_NAME;

	/** The {@link WebServiceMessageReceiverHandlerAdapter} used by this servlet. */
	@SuppressWarnings("NullAway.Init")
	private WebServiceMessageReceiverHandlerAdapter messageReceiverHandlerAdapter;

	private String wsdlDefinitionHandlerAdapterBeanName = DEFAULT_WSDL_DEFINITION_HANDLER_ADAPTER_BEAN_NAME;

	/** The {@link WsdlDefinitionHandlerAdapter} used by this servlet. */
	@SuppressWarnings("NullAway.Init")
	private WsdlDefinitionHandlerAdapter wsdlDefinitionHandlerAdapter;

	private String xsdSchemaHandlerAdapterBeanName = DEFAULT_XSD_SCHEMA_HANDLER_ADAPTER_BEAN_NAME;

	/** The {@link XsdSchemaHandlerAdapter} used by this servlet. */
	@SuppressWarnings("NullAway.Init")
	private XsdSchemaHandlerAdapter xsdSchemaHandlerAdapter;

	private String messageReceiverBeanName = DEFAULT_MESSAGE_RECEIVER_BEAN_NAME;

	/** The {@link WebServiceMessageReceiver} used by this servlet. */
	@SuppressWarnings("NullAway.Init")
	private WebServiceMessageReceiver messageReceiver;

	/** Keys are bean names, values are {@link WsdlDefinition WsdlDefinitions}. */
	private Map<String, WsdlDefinition> wsdlDefinitions = Collections.emptyMap();

	private Map<String, XsdSchema> xsdSchemas = Collections.emptyMap();

	private boolean transformWsdlLocations = false;

	private boolean transformSchemaLocations = false;

	/**
	 * Public constructor, necessary for some Web application servers.
	 */
	public MessageDispatcherServlet() {
		super();
		this.defaultStrategiesHelper = new DefaultStrategiesHelper(MessageDispatcherServlet.class);
	}

	/**
	 * Constructor to support programmatic configuration of the Servlet with the specified
	 * web application context. This constructor is useful in Servlet 3.0+ environments
	 * where instance-based registration of servlets is possible through the
	 * {@code ServletContext#addServlet} API.
	 * <p>
	 * Using this constructor indicates that the following properties / init-params will
	 * be ignored:
	 * <ul>
	 * <li>{@link #setContextClass(Class)} / 'contextClass'</li>
	 * <li>{@link #setContextConfigLocation(String)} / 'contextConfigLocation'</li>
	 * <li>{@link #setContextAttribute(String)} / 'contextAttribute'</li>
	 * <li>{@link #setNamespace(String)} / 'namespace'</li>
	 * </ul>
	 * <p>
	 * The given web application context may or may not yet be
	 * {@linkplain org.springframework.web.context.ConfigurableWebApplicationContext#refresh()
	 * refreshed}. If it has <strong>not</strong> already been refreshed (the recommended
	 * approach), then the following will occur:
	 * <ul>
	 * <li>If the given context does not already have a
	 * {@linkplain org.springframework.web.context.ConfigurableWebApplicationContext#setParent
	 * parent}, the root application context will be set as the parent.</li>
	 * <li>If the given context has not already been assigned an
	 * {@linkplain org.springframework.web.context.ConfigurableWebApplicationContext#setId
	 * id}, one will be assigned to it</li>
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * <li>{@link #postProcessWebApplicationContext} will be called</li>
	 * <li>Any {@code ApplicationContextInitializer}s specified through the
	 * "contextInitializerClasses" init-param or through the
	 * {@link #setContextInitializers} property will be applied.</li>
	 * <li>{@link org.springframework.web.context.ConfigurableWebApplicationContext#refresh
	 * refresh()} will be called if the context implements
	 * {@link org.springframework.web.context.ConfigurableWebApplicationContext}</li>
	 * </ul>
	 * If the context has already been refreshed, none of the above will occur, under the
	 * assumption that the user has performed these actions (or not) per their specific
	 * needs.
	 * <p>
	 * See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * @param webApplicationContext the context to use
	 * @see FrameworkServlet#FrameworkServlet(WebApplicationContext)
	 * @see org.springframework.web.WebApplicationInitializer
	 * @see #initWebApplicationContext()
	 * @see #configureAndRefreshWebApplicationContext(org.springframework.web.context.ConfigurableWebApplicationContext)
	 */
	public MessageDispatcherServlet(WebApplicationContext webApplicationContext) {
		super(webApplicationContext);
		this.defaultStrategiesHelper = new DefaultStrategiesHelper(MessageDispatcherServlet.class);
	}

	/** Returns the bean name used to lookup a {@link WebServiceMessageFactory}. */
	public String getMessageFactoryBeanName() {
		return this.messageFactoryBeanName;
	}

	/**
	 * Sets the bean name used to lookup a {@link WebServiceMessageFactory}. Defaults to
	 * {@link #DEFAULT_MESSAGE_FACTORY_BEAN_NAME}.
	 */
	public void setMessageFactoryBeanName(String messageFactoryBeanName) {
		this.messageFactoryBeanName = messageFactoryBeanName;
	}

	/** Returns the bean name used to lookup a {@link WebServiceMessageReceiver}. */
	public String getMessageReceiverBeanName() {
		return this.messageReceiverBeanName;
	}

	/**
	 * Sets the bean name used to lookup a {@link WebServiceMessageReceiver}. Defaults to
	 * {@link #DEFAULT_MESSAGE_RECEIVER_BEAN_NAME}.
	 */
	public void setMessageReceiverBeanName(String messageReceiverBeanName) {
		this.messageReceiverBeanName = messageReceiverBeanName;
	}

	/**
	 * Indicates whether relative address locations in the WSDL are to be transformed
	 * using the request URI of the incoming {@link HttpServletRequest}.
	 */
	public boolean isTransformWsdlLocations() {
		return this.transformWsdlLocations;
	}

	/**
	 * Sets whether relative address locations in the WSDL are to be transformed using the
	 * request URI of the incoming {@link HttpServletRequest}. Defaults to {@code false}.
	 */
	public void setTransformWsdlLocations(boolean transformWsdlLocations) {
		this.transformWsdlLocations = transformWsdlLocations;
	}

	/**
	 * Indicates whether relative address locations in the XSD are to be transformed using
	 * the request URI of the incoming {@link HttpServletRequest}.
	 */
	public boolean isTransformSchemaLocations() {
		return this.transformSchemaLocations;
	}

	/**
	 * Sets whether relative address locations in the XSD are to be transformed using the
	 * request URI of the incoming {@link HttpServletRequest}. Defaults to {@code false}.
	 */
	public void setTransformSchemaLocations(boolean transformSchemaLocations) {
		this.transformSchemaLocations = transformSchemaLocations;
	}

	/**
	 * Returns the bean name used to lookup a
	 * {@link WebServiceMessageReceiverHandlerAdapter}.
	 */
	public String getMessageReceiverHandlerAdapterBeanName() {
		return this.messageReceiverHandlerAdapterBeanName;
	}

	/**
	 * Sets the bean name used to lookup a
	 * {@link WebServiceMessageReceiverHandlerAdapter}. Defaults to
	 * {@link #DEFAULT_MESSAGE_RECEIVER_HANDLER_ADAPTER_BEAN_NAME}.
	 */
	public void setMessageReceiverHandlerAdapterBeanName(String messageReceiverHandlerAdapterBeanName) {
		this.messageReceiverHandlerAdapterBeanName = messageReceiverHandlerAdapterBeanName;
	}

	/** Returns the bean name used to lookup a {@link WsdlDefinitionHandlerAdapter}. */
	public String getWsdlDefinitionHandlerAdapterBeanName() {
		return this.wsdlDefinitionHandlerAdapterBeanName;
	}

	/**
	 * Sets the bean name used to lookup a {@link WsdlDefinitionHandlerAdapter}. Defaults
	 * to {@link #DEFAULT_WSDL_DEFINITION_HANDLER_ADAPTER_BEAN_NAME}.
	 */
	public void setWsdlDefinitionHandlerAdapterBeanName(String wsdlDefinitionHandlerAdapterBeanName) {
		this.wsdlDefinitionHandlerAdapterBeanName = wsdlDefinitionHandlerAdapterBeanName;
	}

	/** Returns the bean name used to lookup a {@link XsdSchemaHandlerAdapter}. */
	public String getXsdSchemaHandlerAdapterBeanName() {
		return this.xsdSchemaHandlerAdapterBeanName;
	}

	/**
	 * Sets the bean name used to lookup a {@link XsdSchemaHandlerAdapter}. Defaults to
	 * {@link #DEFAULT_XSD_SCHEMA_HANDLER_ADAPTER_BEAN_NAME}.
	 */
	public void setXsdSchemaHandlerAdapterBeanName(String xsdSchemaHandlerAdapterBeanName) {
		this.xsdSchemaHandlerAdapterBeanName = xsdSchemaHandlerAdapterBeanName;
	}

	@Override
	protected void doService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws Exception {
		WsdlDefinition definition = getWsdlDefinition(httpServletRequest);
		if (definition != null) {
			this.wsdlDefinitionHandlerAdapter.handle(httpServletRequest, httpServletResponse, definition);
			return;
		}
		XsdSchema schema = getXsdSchema(httpServletRequest);
		if (schema != null) {
			this.xsdSchemaHandlerAdapter.handle(httpServletRequest, httpServletResponse, schema);
			return;
		}
		this.messageReceiverHandlerAdapter.handle(httpServletRequest, httpServletResponse, this.messageReceiver);
	}

	/**
	 * This implementation calls {@link #initStrategies}.
	 */
	@Override
	protected void onRefresh(ApplicationContext context) {
		initStrategies(context);
	}

	/** Returns the {@link WebServiceMessageReceiver} used by this servlet. */
	protected WebServiceMessageReceiver getMessageReceiver() {
		return this.messageReceiver;
	}

	/**
	 * Determines the {@link WsdlDefinition} for a given request, or {@code null} if none
	 * is found.
	 * <p>
	 * Default implementation checks whether the request method is {@code GET}, whether
	 * the request uri ends with {@code ".wsdl"}, and if there is a {@code WsdlDefinition}
	 * with the same name as the filename in the request uri.
	 * @param request the {@code HttpServletRequest}
	 * @return a definition, or {@code null}
	 */
	protected @Nullable WsdlDefinition getWsdlDefinition(HttpServletRequest request) {
		if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())
				&& request.getRequestURI().endsWith(WSDL_SUFFIX_NAME)) {
			String fileName = WebUtils.extractFilenameFromUrlPath(request.getRequestURI());
			return this.wsdlDefinitions.get(fileName);
		}
		else {
			return null;
		}
	}

	/**
	 * Determines the {@link XsdSchema} for a given request, or {@code null} if none is
	 * found.
	 * <p>
	 * Default implementation checks whether the request method is {@code GET}, whether
	 * the request uri ends with {@code ".xsd"}, and if there is a {@code XsdSchema} with
	 * the same name as the filename in the request uri.
	 * @param request the {@code HttpServletRequest}
	 * @return a schema, or {@code null}
	 */
	protected @Nullable XsdSchema getXsdSchema(HttpServletRequest request) {
		if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())
				&& request.getRequestURI().endsWith(XSD_SUFFIX_NAME)) {
			String fileName = WebUtils.extractFilenameFromUrlPath(request.getRequestURI());
			return this.xsdSchemas.get(fileName);
		}
		else {
			return null;
		}
	}

	/**
	 * Initialize the strategy objects that this servlet uses.
	 * <p>
	 * May be overridden in subclasses in order to initialize further strategy objects.
	 */
	protected void initStrategies(ApplicationContext context) {
		initMessageReceiverHandlerAdapter(context);
		initWsdlDefinitionHandlerAdapter(context);
		initXsdSchemaHandlerAdapter(context);
		initMessageReceiver(context);
		initWsdlDefinitions(context);
		initXsdSchemas(context);
	}

	private void initMessageReceiverHandlerAdapter(ApplicationContext context) {
		try {
			this.messageReceiverHandlerAdapter = context.getBean(getMessageReceiverHandlerAdapterBeanName(),
					WebServiceMessageReceiverHandlerAdapter.class);
		}
		catch (NoSuchBeanDefinitionException ignored) {
			try {
				WebServiceMessageReceiverHandlerAdapter adapter = new WebServiceMessageReceiverHandlerAdapter();
				if (this.logger.isTraceEnabled()) {
					this.logger.trace(
							"No WebServiceMessageReceiverHandlerAdapter '" + getMessageReceiverHandlerAdapterBeanName()
									+ "': using default [" + adapter.getClass().getSimpleName() + "]");
				}
				adapter.setMessageFactory(getWebServiceMessageFactory(context));
				adapter.afterPropertiesSet();
				this.messageReceiverHandlerAdapter = adapter;
			}
			catch (Exception ex) {
				throw new BeanInitializationException(
						"Could not initialize default WebServiceMessageReceiverHandlerAdapter", ex);
			}
		}
	}

	private WebServiceMessageFactory getWebServiceMessageFactory(ApplicationContext context) {
		try {
			return context.getBean(getMessageFactoryBeanName(), WebServiceMessageFactory.class);
		}
		catch (NoSuchBeanDefinitionException ignored) {
			WebServiceMessageFactory factory = this.defaultStrategiesHelper
				.getDefaultStrategy(WebServiceMessageFactory.class, context);
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No WebServiceMessageFactory '" + getMessageFactoryBeanName() + "': using default ["
						+ factory.getClass().getSimpleName() + "]");
			}
			return factory;
		}
	}

	private void initWsdlDefinitionHandlerAdapter(ApplicationContext context) {
		try {
			this.wsdlDefinitionHandlerAdapter = context.getBean(getWsdlDefinitionHandlerAdapterBeanName(),
					WsdlDefinitionHandlerAdapter.class);
		}
		catch (NoSuchBeanDefinitionException ignored) {
			try {
				WsdlDefinitionHandlerAdapter adapter = new WsdlDefinitionHandlerAdapter();
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("No WsdlDefinitionHandlerAdapter '" + getWsdlDefinitionHandlerAdapterBeanName()
							+ "': using default [" + adapter.getClass().getSimpleName() + "]");
				}
				adapter.setTransformLocations(isTransformWsdlLocations());
				adapter.setTransformSchemaLocations(isTransformSchemaLocations());
				adapter.afterPropertiesSet();
				this.wsdlDefinitionHandlerAdapter = adapter;
			}
			catch (Exception ex) {
				throw new BeanInitializationException("Could not initialize default WsdlDefinitionHandlerAdapter", ex);
			}
		}
	}

	private void initXsdSchemaHandlerAdapter(ApplicationContext context) {
		try {
			this.xsdSchemaHandlerAdapter = context.getBean(getXsdSchemaHandlerAdapterBeanName(),
					XsdSchemaHandlerAdapter.class);
		}
		catch (NoSuchBeanDefinitionException ignored) {
			try {
				XsdSchemaHandlerAdapter adapter = new XsdSchemaHandlerAdapter();
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("No XsdSchemaHandlerAdapter '" + getXsdSchemaHandlerAdapterBeanName()
							+ "': using default [" + adapter.getClass().getSimpleName() + "]");
				}
				adapter.setTransformSchemaLocations(isTransformSchemaLocations());
				adapter.afterPropertiesSet();
				this.xsdSchemaHandlerAdapter = adapter;
			}
			catch (Exception ex) {
				throw new BeanInitializationException("Could not initialize default XsdSchemaHandlerAdapter", ex);
			}
		}
	}

	private void initMessageReceiver(ApplicationContext context) {
		try {
			this.messageReceiver = context.getBean(getMessageReceiverBeanName(), WebServiceMessageReceiver.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			WebServiceMessageReceiver receiver = this.defaultStrategiesHelper
				.getDefaultStrategy(WebServiceMessageReceiver.class, context);
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No WebServiceMessageReceiver '" + getMessageReceiverBeanName() + "': using default ["
						+ receiver.getClass().getSimpleName() + "]");
			}
			if (receiver instanceof BeanNameAware beanNameAware && getServletName() != null) {
				beanNameAware.setBeanName(getServletName());
			}
			this.messageReceiver = receiver;
		}
	}

	private void initWsdlDefinitions(ApplicationContext context) {
		this.wsdlDefinitions = BeanFactoryUtils
			.beansOfTypeIncludingAncestors(context, WsdlDefinition.class, true, false)
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
					(entry) -> (entry.getValue().getName() != null) ? entry.getValue().getName() : entry.getKey(),
					Map.Entry::getValue));
		if (this.logger.isDebugEnabled()) {
			for (Map.Entry<String, WsdlDefinition> entry : this.wsdlDefinitions.entrySet()) {
				String name = entry.getKey();
				WsdlDefinition definition = entry.getValue();
				this.logger.debug("Published [" + definition + "] as " + name + WSDL_SUFFIX_NAME);
			}
		}
	}

	private void initXsdSchemas(ApplicationContext context) {
		this.xsdSchemas = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, XsdSchema.class, true, false)
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
					(entry) -> (entry.getValue().getName() != null) ? entry.getValue().getName() : entry.getKey(),
					Map.Entry::getValue));
		if (this.logger.isDebugEnabled()) {
			for (Map.Entry<String, XsdSchema> entry : this.xsdSchemas.entrySet()) {
				String name = entry.getKey();
				XsdSchema schema = entry.getValue();
				this.logger.debug("Published [" + schema + "] as " + name + XSD_SUFFIX_NAME);
			}
		}
	}

}
