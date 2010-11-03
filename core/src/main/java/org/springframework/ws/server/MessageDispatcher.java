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

package org.springframework.ws.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.OrderComparator;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.server.endpoint.PayloadEndpoint;
import org.springframework.ws.server.endpoint.adapter.MessageEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.PayloadEndpointAdapter;
import org.springframework.ws.soap.server.SoapMessageDispatcher;
import org.springframework.ws.support.DefaultStrategiesHelper;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Central dispatcher for use within Spring-WS, dispatching Web service messages to registered endpoints.
 * <p/>
 * This dispatcher is quite similar to Spring MVCs {@link DispatcherServlet}. Just like its counterpart, this dispatcher
 * is very flexible. This class is SOAP agnostic; in typical SOAP Web Services, the {@link SoapMessageDispatcher}
 * subclass is used.
 * <ul>
 * <li>It can use any {@link EndpointMapping} implementation - whether standard, or provided as
 * part of an application - to control the routing of request messages to endpoint objects. Endpoint mappings can be
 * registered using the {@link #setEndpointMappings(List) endpointMappings} property.</li>
 * <li>It can use any {@link EndpointAdapter}; this allows one to use any endpoint interface or form. Defaults to
 * the {@link MessageEndpointAdapter} and {@link PayloadEndpointAdapter}, for {@link MessageEndpoint} and
 * {@link PayloadEndpoint}, respectively, and the
 * {@link org.springframework.ws.server.endpoint.adapter.MessageMethodEndpointAdapter MessageMethodEndpointAdapter} and
 * {@link org.springframework.ws.server.endpoint.adapter.PayloadMethodEndpointAdapter PayloadMethodEndpointAdapter}.
 * Additional endpoint adapters can be added through the {@link #setEndpointAdapters(List) endpointAdapters} property.</li>
 * <li>Its exception resolution strategy can be specified via a
 * {@link EndpointExceptionResolver}, for example mapping certain exceptions to SOAP Faults. Default is none. Additional
 * exception resolvers can be added through the {@link #setEndpointExceptionResolvers(List) endpointExceptionResolvers}
 * property.</li>
 * </ul>
 *
 * @author Arjen Poutsma
 * @see EndpointMapping
 * @see EndpointAdapter
 * @see EndpointExceptionResolver
 * @see org.springframework.web.servlet.DispatcherServlet
 * @since 1.0.0
 */
public class MessageDispatcher implements WebServiceMessageReceiver, BeanNameAware, ApplicationContextAware {

    /** Logger available to subclasses. */
    protected final Log logger = LogFactory.getLog(getClass());

    /** Log category to use when no mapped endpoint is found for a request. */
    public static final String ENDPOINT_NOT_FOUND_LOG_CATEGORY = "org.springframework.ws.server.EndpointNotFound";

    /** Additional logger to use when no mapped endpoint is found for a request. */
    protected static final Log endpointNotFoundLogger =
            LogFactory.getLog(MessageDispatcher.ENDPOINT_NOT_FOUND_LOG_CATEGORY);

    /** Log category to use for message tracing. */
    public static final String MESSAGE_TRACING_LOG_CATEGORY = "org.springframework.ws.server.MessageTracing";

    /** Additional logger to use for sent message tracing. */
    protected static final Log sentMessageTracingLogger =
            LogFactory.getLog(MessageDispatcher.MESSAGE_TRACING_LOG_CATEGORY + ".sent");

    /** Additional logger to use for received message tracing. */
    protected static final Log receivedMessageTracingLogger =
            LogFactory.getLog(MessageDispatcher.MESSAGE_TRACING_LOG_CATEGORY + ".received");

    private final DefaultStrategiesHelper defaultStrategiesHelper;

    /** The registered bean name for this dispatcher. */
    private String beanName;

    /** List of EndpointAdapters used in this dispatcher. */
    private List<EndpointAdapter> endpointAdapters;

    /** List of EndpointExceptionResolvers used in this dispatcher. */
    private List<EndpointExceptionResolver> endpointExceptionResolvers;

    /** List of EndpointMappings used in this dispatcher. */
    private List<EndpointMapping> endpointMappings;

    /** Initializes a new instance of the <code>MessageDispatcher</code>. */
    public MessageDispatcher() {
        defaultStrategiesHelper = new DefaultStrategiesHelper(getClass());
    }

    /** Returns the <code>EndpointAdapter</code>s to use by this <code>MessageDispatcher</code>. */
    public List<EndpointAdapter> getEndpointAdapters() {
        return endpointAdapters;
    }

    /** Sets the <code>EndpointAdapter</code>s to use by this <code>MessageDispatcher</code>. */
    public void setEndpointAdapters(List<EndpointAdapter> endpointAdapters) {
        this.endpointAdapters = endpointAdapters;
    }

    /** Returns the <code>EndpointExceptionResolver</code>s to use by this <code>MessageDispatcher</code>. */
    public List<EndpointExceptionResolver> getEndpointExceptionResolvers() {
        return endpointExceptionResolvers;
    }

    /** Sets the <code>EndpointExceptionResolver</code>s to use by this <code>MessageDispatcher</code>. */
    public void setEndpointExceptionResolvers(List<EndpointExceptionResolver> endpointExceptionResolvers) {
        this.endpointExceptionResolvers = endpointExceptionResolvers;
    }

    /** Returns the <code>EndpointMapping</code>s to use by this <code>MessageDispatcher</code>. */
    public List<EndpointMapping> getEndpointMappings() {
        return endpointMappings;
    }

    /** Sets the <code>EndpointMapping</code>s to use by this <code>MessageDispatcher</code>. */
    public void setEndpointMappings(List<EndpointMapping> endpointMappings) {
        this.endpointMappings = endpointMappings;
    }

    public final void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        initEndpointAdapters(applicationContext);
        initEndpointExceptionResolvers(applicationContext);
        initEndpointMappings(applicationContext);
    }

    public void receive(MessageContext messageContext) throws Exception {
        // Let's keep a reference to the request content as it came in, it might be changed by interceptors in dispatch()
        String requestContent = "";
        if (receivedMessageTracingLogger.isTraceEnabled() || sentMessageTracingLogger.isTraceEnabled()) {
            requestContent = getMessageContent(messageContext.getRequest());            
        }
        if (receivedMessageTracingLogger.isTraceEnabled()) {
            receivedMessageTracingLogger.trace("Received request [" + requestContent + "]");
        }
        else if (receivedMessageTracingLogger.isDebugEnabled()) {
            receivedMessageTracingLogger.debug("Received request [" + messageContext.getRequest() + "]");
        }
        dispatch(messageContext);
        if (messageContext.hasResponse()) {
            WebServiceMessage response = messageContext.getResponse();
            if (sentMessageTracingLogger.isTraceEnabled()) {
                String responseContent = getMessageContent(response);
                sentMessageTracingLogger.trace("Sent response [" + responseContent + "] for request [" +
                                requestContent + "]");
            }
            else if (sentMessageTracingLogger.isDebugEnabled()) {
                sentMessageTracingLogger.debug("Sent response [" + response + "] for request [" +
                        messageContext.getRequest() + "]");
            }
        }
        else if (sentMessageTracingLogger.isDebugEnabled()) {
            sentMessageTracingLogger
                    .debug("MessageDispatcher with name '" + beanName + "' sends no response for request [" +
                            messageContext.getRequest() + "]");
        }
    }

    private String getMessageContent(WebServiceMessage message) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        message.writeTo(bos);
        return bos.toString("UTF-8");
    }

    /**
     * Dispatches the request in the given MessageContext according to the configuration.
     *
     * @param messageContext the message context
     * @throws org.springframework.ws.NoEndpointFoundException
     *          thrown when an endpoint cannot be resolved for the incoming message
     */
    protected final void dispatch(MessageContext messageContext) throws Exception {
        EndpointInvocationChain mappedEndpoint = null;
        int interceptorIndex = -1;
        try {
            // Determine endpoint for the current context
            mappedEndpoint = getEndpoint(messageContext);
            if (mappedEndpoint == null || mappedEndpoint.getEndpoint() == null) {
                throw new NoEndpointFoundException(messageContext.getRequest());
            }
            if (!handleRequest(mappedEndpoint, messageContext)) {
                return;
            }
            // Apply handleRequest of registered interceptors
            if (mappedEndpoint.getInterceptors() != null) {
                for (int i = 0; i < mappedEndpoint.getInterceptors().length; i++) {
                    EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                    interceptorIndex = i;
                    if (!interceptor.handleRequest(messageContext, mappedEndpoint.getEndpoint())) {
                        triggerHandleResponse(mappedEndpoint, interceptorIndex, messageContext);
                        return;
                    }
                }
            }
            // Actually invoke the endpoint
            EndpointAdapter endpointAdapter = getEndpointAdapter(mappedEndpoint.getEndpoint());
            endpointAdapter.invoke(messageContext, mappedEndpoint.getEndpoint());

            // Apply handleResponse methods of registered interceptors
            triggerHandleResponse(mappedEndpoint, interceptorIndex, messageContext);
        }
        catch (NoEndpointFoundException ex) {
            // No triggering of interceptors if no endpoint is found
            if (endpointNotFoundLogger.isWarnEnabled()) {
                endpointNotFoundLogger.warn("No endpoint mapping found for [" + messageContext.getRequest() + "]");
            }
            throw ex;
        }
        catch (Exception ex) {
            Object endpoint = mappedEndpoint != null ? mappedEndpoint.getEndpoint() : null;
            processEndpointException(messageContext, endpoint, ex);
            triggerHandleResponse(mappedEndpoint, interceptorIndex, messageContext);
        }
    }

    /**
     * Returns the endpoint for this request. All endpoint mappings are tried, in order.
     *
     * @return the <code>EndpointInvocationChain</code>, or <code>null</code> if no endpoint could be found.
     */
    protected EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception {
        for (EndpointMapping endpointMapping : getEndpointMappings()) {
            EndpointInvocationChain endpoint = endpointMapping.getEndpoint(messageContext);
            if (endpoint != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Endpoint mapping [" + endpointMapping + "] maps request to endpoint [" +
                            endpoint.getEndpoint() + "]");
                }
                return endpoint;
            }
            else if (logger.isDebugEnabled()) {
                logger.debug("Endpoint mapping [" + endpointMapping + "] has no mapping for request");
            }
        }
        return null;
    }

    /**
     * Returns the <code>EndpointAdapter</code> for the given endpoint.
     *
     * @param endpoint the endpoint to find an adapter for
     * @return the adapter
     */
    protected EndpointAdapter getEndpointAdapter(Object endpoint) {
        for (EndpointAdapter endpointAdapter : getEndpointAdapters()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Testing endpoint adapter [" + endpointAdapter + "]");
            }
            if (endpointAdapter.supports(endpoint)) {
                return endpointAdapter;
            }
        }
        throw new IllegalStateException("No adapter for endpoint [" + endpoint + "]: Does your endpoint implement a " +
                "supported interface like MessageHandler or PayloadEndpoint?");
    }

    /**
     * Callback for pre-processing of given invocation chain and message context. Gets called before invocation of
     * <code>handleRequest</code> on the interceptors.
     * <p/>
     * Default implementation does nothing, and returns <code>true</code>.
     *
     * @param mappedEndpoint the mapped <code>EndpointInvocationChain</code>
     * @param messageContext the message context
     * @return <code>true</code> if processing should continue; <code>false</code> otherwise
     */
    protected boolean handleRequest(EndpointInvocationChain mappedEndpoint, MessageContext messageContext) {
        return true;
    }

    /**
     * Determine an error <code>SOAPMessage</code> response via the registered <code>EndpointExceptionResolvers</code>.
     * Most likely, the response contains a <code>SOAPFault</code>. If no suitable resolver was found, the exception is
     * rethrown.
     *
     * @param messageContext current SOAPMessage request
     * @param endpoint       the executed endpoint, or null if none chosen at the time of the exception
     * @param ex             the exception that got thrown during handler execution
     * @throws Exception if no suitable resolver is found
     */
    protected void processEndpointException(MessageContext messageContext, Object endpoint, Exception ex)
            throws Exception {
        for (EndpointExceptionResolver resolver : getEndpointExceptionResolvers()) {
            if (resolver.resolveException(messageContext, endpoint, ex)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Endpoint invocation resulted in exception - responding with Fault", ex);
                }
                return;
            }
        }
        // exception not resolved
        throw ex;
    }

    /**
     * Trigger handleResponse or handleFault on the mapped EndpointInterceptors. Will just invoke said method on all
     * interceptors whose handleRequest invocation returned <code>true</code>, in addition to the last interceptor who
     * returned <code>false</code>.
     *
     * @param mappedEndpoint   the mapped EndpointInvocationChain
     * @param interceptorIndex index of last interceptor that was called
     * @param messageContext   the message context, whose request and response are filled
     * @see EndpointInterceptor#handleResponse(MessageContext,Object)
     * @see EndpointInterceptor#handleFault(MessageContext, Object)
     */
    private void triggerHandleResponse(EndpointInvocationChain mappedEndpoint,
                                       int interceptorIndex,
                                       MessageContext messageContext) throws Exception {
        if (mappedEndpoint != null && messageContext.hasResponse() &&
                !ObjectUtils.isEmpty(mappedEndpoint.getInterceptors())) {
            boolean hasFault = false;
            WebServiceMessage response = messageContext.getResponse();
            if (response instanceof FaultAwareWebServiceMessage) {
                hasFault = ((FaultAwareWebServiceMessage) response).hasFault();
            }
            boolean resume = true;
            for (int i = interceptorIndex; resume && i >= 0; i--) {
                EndpointInterceptor interceptor = mappedEndpoint.getInterceptors()[i];
                if (!hasFault) {
                    resume = interceptor.handleResponse(messageContext, mappedEndpoint.getEndpoint());
                }
                else {
                    resume = interceptor.handleFault(messageContext, mappedEndpoint.getEndpoint());
                }
            }
        }
    }

    /**
     * Initialize the <code>EndpointAdapters</code> used by this class. If no adapter beans are explicitly set by using
     * the <code>endpointAdapters</code> property, we use the default strategies.
     *
     * @see #setEndpointAdapters(java.util.List)
     */
    private void initEndpointAdapters(ApplicationContext applicationContext) throws BeansException {
        if (endpointAdapters == null) {
            Map<String, EndpointAdapter> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, EndpointAdapter.class, true, false);
            if (!matchingBeans.isEmpty()) {
                endpointAdapters = new ArrayList<EndpointAdapter>(matchingBeans.values());
                Collections.sort(endpointAdapters, new OrderComparator());
            }
            else {
                endpointAdapters =
                        defaultStrategiesHelper.getDefaultStrategies(EndpointAdapter.class, applicationContext);
                if (logger.isDebugEnabled()) {
                    logger.debug("No EndpointAdapters found, using defaults");
                }
            }
        }
    }

    /**
     * Initialize the <code>EndpointExceptionResolver</code> used by this class. If no resolver beans are explicitly set
     * by using the <code>endpointExceptionResolvers</code> property, we use the default strategies.
     *
     * @see #setEndpointExceptionResolvers(java.util.List)
     */
    private void initEndpointExceptionResolvers(ApplicationContext applicationContext) throws BeansException {
        if (endpointExceptionResolvers == null) {
            Map<String, EndpointExceptionResolver> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, EndpointExceptionResolver.class, true, false);
            if (!matchingBeans.isEmpty()) {
                endpointExceptionResolvers = new ArrayList<EndpointExceptionResolver>(matchingBeans.values());
                Collections.sort(endpointExceptionResolvers, new OrderComparator());
            }
            else {
                endpointExceptionResolvers = defaultStrategiesHelper
                        .getDefaultStrategies(EndpointExceptionResolver.class, applicationContext);
                if (logger.isDebugEnabled()) {
                    logger.debug("No EndpointExceptionResolvers found, using defaults");
                }
            }
        }
    }

    /**
     * Initialize the <code>EndpointMappings</code> used by this class. If no mapping beans are explictely set by using
     * the <code>endpointMappings</code> property, we use the default strategies.
     *
     * @see #setEndpointMappings(java.util.List)
     */
    private void initEndpointMappings(ApplicationContext applicationContext) throws BeansException {
        if (endpointMappings == null) {
            Map<String, EndpointMapping> matchingBeans = BeanFactoryUtils
                    .beansOfTypeIncludingAncestors(applicationContext, EndpointMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                endpointMappings = new ArrayList<EndpointMapping>(matchingBeans.values());
                Collections.sort(endpointMappings, new OrderComparator());
            }
            else {
                endpointMappings =
                        defaultStrategiesHelper.getDefaultStrategies(EndpointMapping.class, applicationContext);
                if (logger.isDebugEnabled()) {
                    logger.debug("No EndpointMappings found, using defaults");
                }
            }
        }
    }
}
