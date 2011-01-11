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

package org.springframework.ws.server.endpoint;

import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for {@link EndpointExceptionResolver EndpointExceptionResolvers}.
 * <p/>
 * <p>Provides a set of mapped endpoints that the resolver should map.
 *
 * @author Arjen Poutsma
 * @author Tareq Abed Rabbo
 * @since 1.0.0
 */
public abstract class AbstractEndpointExceptionResolver implements EndpointExceptionResolver, Ordered {

    /** Shared {@link Log} for subclasses to use. */
    protected final Log logger = LogFactory.getLog(getClass());

    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

    private Set<?> mappedEndpoints;

    private Log warnLogger;

    /**
     * Specify the set of endpoints that this exception resolver should map. <p>The exception mappings and the default
     * fault will only apply to the specified endpoints.
     * <p/>
     * If no endpoints are set, both the exception mappings and the default fault will apply to all handlers. This means
     * that a specified default fault will be used as fallback for all exceptions; any further
     * <code>EndpointExceptionResolvers</code> in the chain will be ignored in this case.
     */
    public void setMappedEndpoints(Set<?> mappedEndpoints) {
        this.mappedEndpoints = mappedEndpoints;
    }

    /**
     * Set the log category for warn logging. The name will be passed to the underlying logger implementation through
     * Commons Logging, getting interpreted as log category according to the logger's configuration.
     * <p/>
     * Default is no warn logging. Specify this setting to activate warn logging into a specific category.
     * Alternatively, override the {@link #logException} method for custom logging.
     *
     * @see org.apache.commons.logging.LogFactory#getLog(String)
     * @see org.apache.log4j.Logger#getLogger(String)
     * @see java.util.logging.Logger#getLogger(String)
     */
    public void setWarnLogCategory(String loggerName) {
        this.warnLogger = LogFactory.getLog(loggerName);
    }

    /**
     * Specify the order value for this mapping.
     * <p/>
     * Default value is {@link Integer#MAX_VALUE}, meaning that it's non-ordered.
     *
     * @see org.springframework.core.Ordered#getOrder()
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    public final int getOrder() {
        return order;
    }

    /**
     * Default implementation that checks whether the given <code>endpoint</code> is in the set of {@link
     * #setMappedEndpoints mapped endpoints}.
     *
     * @see #resolveExceptionInternal(MessageContext,Object,Exception)
     */
    public final boolean resolveException(MessageContext messageContext, Object endpoint, Exception ex) {
        Object mappedEndpoint = endpoint instanceof MethodEndpoint ? ((MethodEndpoint) endpoint).getBean() : endpoint;
        if (mappedEndpoints != null && !mappedEndpoints.contains(mappedEndpoint)) {
            return false;
        }
        // Log exception, both at debug log level and at warn level, if desired.
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving exception from endpoint [" + endpoint + "]: " + ex);
        }
        logException(ex, messageContext);
        return resolveExceptionInternal(messageContext, endpoint, ex);
    }

    /**
     * Log the given exception at warn level, provided that warn logging has been activated through the {@link
     * #setWarnLogCategory "warnLogCategory"} property.
     * <p/>
     * Calls {@link #buildLogMessage} in order to determine the concrete message to log. Always passes the full
     * exception to the logger.
     *
     * @param ex             the exception that got thrown during handler execution
     * @param messageContext current message context request
     * @see #setWarnLogCategory
     * @see #buildLogMessage
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    protected void logException(Exception ex, MessageContext messageContext) {
        if (this.warnLogger != null && this.warnLogger.isWarnEnabled()) {
            this.warnLogger.warn(buildLogMessage(ex, messageContext), ex);
        }
    }

    /**
     * Build a log message for the given exception, occured during processing the given message context.
     *
     * @param ex             the exception that got thrown during handler execution
     * @param messageContext the message context
     * @return the log message to use
     */
    protected String buildLogMessage(Exception ex, MessageContext messageContext) {
        return "Endpoint execution resulted in exception";
    }

    /**
     * Template method for resolving exceptions that is called by {@link #resolveException}.
     *
     * @param messageContext current message context
     * @param endpoint       the executed endpoint, or <code>null</code> if none chosen at the time of the exception
     * @param ex             the exception that got thrown during endpoint execution
     * @return <code>true</code> if resolved; <code>false</code> otherwise
     * @see #resolveException(MessageContext,Object,Exception)
     */
    protected abstract boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex);

}
