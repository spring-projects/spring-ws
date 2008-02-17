/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.transport.http;

import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.util.WebUtils;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.server.EndpointAdapter;
import org.springframework.ws.server.EndpointExceptionResolver;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.support.DefaultStrategiesHelper;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.wsdl.WsdlDefinition;

/**
 * Servlet for simplified dispatching of Web service messages.
 * <p/>
 * This servlet is a convenient alternative to the standard Spring-MVC {@link DispatcherServlet} with separate {@link
 * WebServiceMessageReceiverHandlerAdapter}, {@link MessageDispatcher}, and {@link WsdlDefinitionHandlerAdapter}
 * instances.
 * <p/>
 * This servlet automatically detects {@link EndpointAdapter EndpointAdapters}, {@link EndpointMapping
 * EndpointMappings}, and {@link EndpointExceptionResolver EndpointExceptionResolvers} <i>by type</i>.
 * <p/>
 * This servlet also automatically detects any {@link WsdlDefinition} defined in its application context. This WSDL is
 * exposed under the bean name: for example, a <code>WsdlDefinition</code> bean named '<code>echo</code>' will be
 * exposed as <code>echo.wsdl</code> in this servlet's context: <code>http://localhost:8080/spring-ws/echo.wsdl</code>.
 * When the <code>transformWsdlLocations</code> init-param is set to <code>true</code> in this servlet's configuration
 * in <code>web.xml</code>, all <code>location</code> attributes in the WSDL definitions will reflect the URL of the
 * incoming request.
 *
 * @author Arjen Poutsma
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.ws.server.MessageDispatcher
 * @see org.springframework.ws.transport.http.WebServiceMessageReceiverHandlerAdapter
 * @since 1.0.0
 */
public class MessageDispatcherServlet extends FrameworkServlet {

    /** Well-known name for the {@link WebServiceMessageFactory} bean in the bean factory for this namespace. */
    public static final String DEFAULT_MESSAGE_FACTORY_BEAN_NAME = "messageFactory";

    /** Well-known name for the {@link WebServiceMessageReceiver} object in the bean factory for this namespace. */
    public static final String DEFAULT_MESSAGE_RECEIVER_BEAN_NAME = "messageReceiver";

    /**
     * Well-known name for the {@link WebServiceMessageReceiverHandlerAdapter} object in the bean factory for this
     * namespace.
     */
    public static final String DEFAULT_MESSAGE_RECEIVER_HANDLER_ADAPTER_BEAN_NAME = "messageReceiverHandlerAdapter";

    /** Well-known name for the {@link WsdlDefinitionHandlerAdapter} object in the bean factory for this namespace. */
    public static final String DEFAULT_WSDL_DEFINITION_HANDLER_ADAPTER_BEAN_NAME = "wsdlDefinitionHandlerAdapter";

    /**
     * Name of the class path resource (relative to the {@link MessageDispatcherServlet} class) that defines
     * <code>MessageDispatcherServlet's</code> default strategy names.
     */
    private static final String DEFAULT_STRATEGIES_PATH = "MessageDispatcherServlet.properties";

    /** Suffix of a WSDL request uri. */
    private static final String WSDL_SUFFIX_NAME = ".wsdl";

    private final DefaultStrategiesHelper defaultStrategiesHelper;

    private String messageFactoryBeanName = DEFAULT_MESSAGE_FACTORY_BEAN_NAME;

    private String messageReceiverHandlerAdapterBeanName = DEFAULT_MESSAGE_RECEIVER_HANDLER_ADAPTER_BEAN_NAME;

    /** The {@link WebServiceMessageReceiverHandlerAdapter} used by this servlet. */
    private WebServiceMessageReceiverHandlerAdapter messageReceiverHandlerAdapter;

    private String wsdlDefinitionHandlerAdapterBeanName = DEFAULT_WSDL_DEFINITION_HANDLER_ADAPTER_BEAN_NAME;

    /** The {@link WsdlDefinitionHandlerAdapter} used by this servlet. */
    private WsdlDefinitionHandlerAdapter wsdlDefinitionHandlerAdapter;

    private String messageReceiverBeanName = DEFAULT_MESSAGE_RECEIVER_BEAN_NAME;

    /** The {@link WebServiceMessageReceiver} used by this servlet. */
    private WebServiceMessageReceiver messageReceiver;

    /** Keys are bean names, values are {@link WsdlDefinition WsdlDefinitions}. */
    private Map wsdlDefinitions;

    private boolean transformWsdlLocations = false;

    /** Public constructor, necessary for some Web application servers. */
    public MessageDispatcherServlet() {
        defaultStrategiesHelper = new DefaultStrategiesHelper(
                new ClassPathResource(DEFAULT_STRATEGIES_PATH, MessageDispatcherServlet.class));
    }

    /** Returns the bean name used to lookup a {@link WebServiceMessageFactory}. */
    public String getMessageFactoryBeanName() {
        return messageFactoryBeanName;
    }

    /**
     * Sets the bean name used to lookup a {@link WebServiceMessageFactory}. Defaults to {@link
     * #DEFAULT_MESSAGE_FACTORY_BEAN_NAME}.
     */
    public void setMessageFactoryBeanName(String messageFactoryBeanName) {
        this.messageFactoryBeanName = messageFactoryBeanName;
    }

    /** Returns the bean name used to lookup a {@link WebServiceMessageReceiver}. */
    public String getMessageReceiverBeanName() {
        return messageReceiverBeanName;
    }

    /**
     * Sets the bean name used to lookup a {@link WebServiceMessageReceiver}. Defaults to {@link
     * #DEFAULT_MESSAGE_RECEIVER_BEAN_NAME}.
     */
    public void setMessageReceiverBeanName(String messageReceiverBeanName) {
        this.messageReceiverBeanName = messageReceiverBeanName;
    }

    /**
     * Indicates whether relative address locations in the WSDL are to be transformed using the request URI of the
     * incoming {@link HttpServletRequest}.
     */
    public boolean isTransformWsdlLocations() {
        return transformWsdlLocations;
    }

    /** Returns the bean name used to lookup a {@link WebServiceMessageReceiverHandlerAdapter}. */
    public String getMessageReceiverHandlerAdapterBeanName() {
        return messageReceiverHandlerAdapterBeanName;
    }

    /**
     * Sets the bean name used to lookup a {@link WebServiceMessageReceiverHandlerAdapter}. Defaults to {@link
     * #DEFAULT_MESSAGE_RECEIVER_HANDLER_ADAPTER_BEAN_NAME}.
     */
    public void setMessageReceiverHandlerAdapterBeanName(String messageReceiverHandlerAdapterBeanName) {
        this.messageReceiverHandlerAdapterBeanName = messageReceiverHandlerAdapterBeanName;
    }

    /** Returns the bean name used to lookup a {@link WsdlDefinitionHandlerAdapter}. */
    public String getWsdlDefinitionHandlerAdapterBeanName() {
        return wsdlDefinitionHandlerAdapterBeanName;
    }

    /**
     * Sets the bean name used to lookup a {@link WsdlDefinitionHandlerAdapter}. Defaults to {@link
     * #DEFAULT_WSDL_DEFINITION_HANDLER_ADAPTER_BEAN_NAME}.
     */
    public void setWsdlDefinitionHandlerAdapterBeanName(String wsdlDefinitionHandlerAdapterBeanName) {
        this.wsdlDefinitionHandlerAdapterBeanName = wsdlDefinitionHandlerAdapterBeanName;
    }

    /**
     * Sets whether relative address locations in the WSDL are to be transformed using the request URI of the incoming
     * {@link HttpServletRequest}. Defaults to <code>false</code>.
     */
    public void setTransformWsdlLocations(boolean transformWsdlLocations) {
        this.transformWsdlLocations = transformWsdlLocations;
    }

    protected void doService(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        WsdlDefinition definition = getWsdlDefinition(httpServletRequest);
        if (definition != null) {
            wsdlDefinitionHandlerAdapter.handle(httpServletRequest, httpServletResponse, definition);
        }
        else {
            messageReceiverHandlerAdapter.handle(httpServletRequest, httpServletResponse, messageReceiver);
        }
    }

    protected void initFrameworkServlet() throws ServletException, BeansException {
        initMessageReceiverHandlerAdapter();
        initWsdlDefinitionHandlerAdapter();
        initMessageReceiver();
        initWsdlDefinitions();
    }

    protected long getLastModified(HttpServletRequest httpServletRequest) {
        WsdlDefinition definition = getWsdlDefinition(httpServletRequest);
        if (definition != null) {
            return wsdlDefinitionHandlerAdapter.getLastModified(httpServletRequest, definition);
        }
        else {
            return messageReceiverHandlerAdapter.getLastModified(httpServletRequest, messageReceiver);
        }
    }

    /** Returns the {@link WebServiceMessageReceiver} used by this servlet. */
    protected WebServiceMessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /**
     * Determines the {@link WsdlDefinition} for a given request, or <code>null</code> if none is found.
     * <p/>
     * Default implementation checks whether the request method is <code>GET</code>, whether the request uri ends with
     * <code>".wsdl"</code>, and if there is a <code>WsdlDefinition</code> with the same name as the filename in the
     * request uri.
     *
     * @param request the <code>HttpServletRequest</code>
     * @return a definition, or <code>null</code>
     */
    protected WsdlDefinition getWsdlDefinition(HttpServletRequest request) {
        if (HttpTransportConstants.METHOD_GET.equals(request.getMethod()) &&
                request.getRequestURI().endsWith(WSDL_SUFFIX_NAME)) {
            String fileName = WebUtils.extractFilenameFromUrlPath(request.getRequestURI());
            return (WsdlDefinition) wsdlDefinitions.get(fileName);
        }
        else {
            return null;
        }
    }

    private void initMessageReceiverHandlerAdapter() {
        try {
            try {
                messageReceiverHandlerAdapter = (WebServiceMessageReceiverHandlerAdapter) getWebApplicationContext()
                        .getBean(getMessageReceiverHandlerAdapterBeanName(),
                                WebServiceMessageReceiverHandlerAdapter.class);
            }
            catch (NoSuchBeanDefinitionException ignored) {
                messageReceiverHandlerAdapter = new WebServiceMessageReceiverHandlerAdapter();
            }
            initWebServiceMessageFactory();
            messageReceiverHandlerAdapter.afterPropertiesSet();
        }
        catch (Exception ex) {
            throw new BeanInitializationException("Could not initialize WebServiceMessageReceiverHandlerAdapter", ex);
        }
    }

    private void initWebServiceMessageFactory() {
        WebServiceMessageFactory messageFactory;
        try {
            messageFactory = (WebServiceMessageFactory) getWebApplicationContext()
                    .getBean(getMessageFactoryBeanName(), WebServiceMessageFactory.class);
        }
        catch (NoSuchBeanDefinitionException ignored) {
            messageFactory = (WebServiceMessageFactory) defaultStrategiesHelper
                    .getDefaultStrategy(WebServiceMessageFactory.class, getWebApplicationContext());
            if (logger.isDebugEnabled()) {
                logger.debug("No WebServiceMessageFactory found in servlet '" + getServletName() + "': using default");
            }
        }
        messageReceiverHandlerAdapter.setMessageFactory(messageFactory);
    }

    private void initWsdlDefinitionHandlerAdapter() {
        try {
            try {
                wsdlDefinitionHandlerAdapter = (WsdlDefinitionHandlerAdapter) getWebApplicationContext()
                        .getBean(getWsdlDefinitionHandlerAdapterBeanName(), WsdlDefinitionHandlerAdapter.class);

            }
            catch (NoSuchBeanDefinitionException ignored) {
                wsdlDefinitionHandlerAdapter = new WsdlDefinitionHandlerAdapter();
            }
            wsdlDefinitionHandlerAdapter.setTransformLocations(isTransformWsdlLocations());
            wsdlDefinitionHandlerAdapter.afterPropertiesSet();
        }
        catch (Exception ex) {
            throw new BeanInitializationException("Could not initialize WsdlDefinitionHandlerAdapter", ex);
        }
    }

    private void initMessageReceiver() {
        try {
            messageReceiver = (WebServiceMessageReceiver) getWebApplicationContext()
                    .getBean(getMessageReceiverBeanName(), WebServiceMessageReceiver.class);
        }
        catch (NoSuchBeanDefinitionException ex) {
            messageReceiver = (WebServiceMessageReceiver) defaultStrategiesHelper
                    .getDefaultStrategy(WebServiceMessageReceiver.class, getWebApplicationContext());
            if (messageReceiver instanceof BeanNameAware) {
                ((BeanNameAware) messageReceiver).setBeanName(getServletName());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("No MessageDispatcher found in servlet '" + getServletName() + "': using default");
            }
        }
    }

    /** Find all {@link WsdlDefinition WsdlDefinitions} in the ApplicationContext, incuding ancestor contexts. */
    private void initWsdlDefinitions() {
        wsdlDefinitions = BeanFactoryUtils
                .beansOfTypeIncludingAncestors(getWebApplicationContext(), WsdlDefinition.class, true, false);
        if (logger.isDebugEnabled()) {
            for (Iterator iterator = wsdlDefinitions.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String beanName = (String) entry.getKey();
                WsdlDefinition definition = (WsdlDefinition) entry.getValue();
                logger.debug("Published [" + definition + "] as " + beanName + WSDL_SUFFIX_NAME);
            }
        }
    }
}
