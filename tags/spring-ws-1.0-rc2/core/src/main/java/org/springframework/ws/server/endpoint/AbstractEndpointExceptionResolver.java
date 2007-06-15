/*
 * Copyright 2006-2007 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointExceptionResolver;

/**
 * Abstract base class for {@link EndpointExceptionResolver EndpointExceptionResolvers}.
 * <p/>
 * <p>Provides a set of mapped endpoints that the resolver should map.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractEndpointExceptionResolver implements EndpointExceptionResolver, Ordered {

    /** Shared {@link Log} for subclasses to use. */
    protected final Log logger = LogFactory.getLog(getClass());

    private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

    private Set mappedEndpoints;

    /**
     * Specify the set of endpoints that this exception resolver should map. <p>The exception mappings and the default
     * fault will only apply to the specified endpoints.
     * <p/>
     * If no endpoints are set, both the exception mappings and the default fault will apply to all handlers. This means
     * that a specified default fault will be used as fallback for all exceptions; any further
     * <code>EndpointExceptionResolvers</code> in the chain will be ignored in this case.
     */
    public void setMappedEndpoints(Set mappedEndpoints) {
        this.mappedEndpoints = mappedEndpoints;
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
     * @see #resolveExceptionInternal(org.springframework.ws.context.MessageContext,Object,Exception)
     */
    public final boolean resolveException(MessageContext messageContext, Object endpoint, Exception ex) {
        if (mappedEndpoints != null && !mappedEndpoints.contains(endpoint)) {
            return false;
        }
        return resolveExceptionInternal(messageContext, endpoint, ex);
    }

    /**
     * Template method for resolving exceptions that is called by {@link #resolveException}.
     *
     * @param messageContext current message context
     * @param endpoint       the executed endpoint, or <code>null</code> if none chosen at the time of the exception
     * @param ex             the exception that got thrown during endpoint execution
     * @return <code>true</code> if resolved; <code>false</code> otherwise
     * @see #resolveException(org.springframework.ws.context.MessageContext,Object,Exception)
     */
    protected abstract boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex);

}
