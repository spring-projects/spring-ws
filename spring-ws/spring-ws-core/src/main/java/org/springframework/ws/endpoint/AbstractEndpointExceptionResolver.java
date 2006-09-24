/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.endpoint;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ws.EndpointExceptionResolver;
import org.springframework.ws.context.MessageContext;

/**
 * Abstract base class for <code>ExceptionResolver</code>s. Provides a set of mapped endpoints that the resolver should
 * map.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractEndpointExceptionResolver implements EndpointExceptionResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    private Set mappedEndpoints;

    /**
     * Specify the set of endpoints that this exception resolver should map. The exception mappings and the default
     * fault will only apply to the specified endpoints.
     * <p/>
     * If no endpoints set, both the exception mappings and the default fault will apply to all handlers. This means
     * that a specified default fault will be used as fallback for all exceptions; any further
     * <code>EndpointExceptionResolvers</code> in the chain will be ignored in this case.
     */
    public void setMappedEndpoints(Set mappedEndpoints) {
        this.mappedEndpoints = mappedEndpoints;
    }

    /**
     * Default implementation. Checks whether the given endpoint is in the set of mapped endpoints. Calls
     * <code>resolveExceptionInternal</code>.
     *
     * @see #resolveExceptionInternal(org.springframework.ws.context.MessageContext, Object, Exception)
     */
    public final boolean resolveException(MessageContext messageContext, Object endpoint, Exception ex) {
        if (this.mappedEndpoints != null && !this.mappedEndpoints.contains(endpoint)) {
            return false;
        }
        return resolveExceptionInternal(messageContext, endpoint, ex);
    }

    /**
     * Template method for resolving exceptions. Gets called after <code>resolveException</code>.
     *
     * @param messageContext current message context
     * @param endpoint       the executed endpoint, or null if none chosen at the time of the exception
     * @param ex             the exception that got thrown during endpoint execution
     * @return <code>true</code> if resolved; <code>false</code> otherwise
     * @see #resolveException(org.springframework.ws.context.MessageContext, Object, Exception)
     */
    protected abstract boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex);


}
