/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;

/**
 * Abstract base class for WS-Addressing <code>Action</code>-mapped {@link org.springframework.ws.server.EndpointMapping}
 * implementations. Provides infrastructure for mapping endpoints to actions.
 * <p/>
 * By default, this mapping creates a <code>Action</code> for reply messages based on the request message, plus the
 * extra {@link #setOutputActionSuffix(String) suffix}, and a   * By default, this mapping creates a <code>Action</code>
 * for reply messages based on the request message, plus the extra {@link #setOutputActionSuffix(String) suffix}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractActionEndpointMapping extends AbstractAddressingEndpointMapping
        implements ApplicationContextAware {

    /** The defaults suffix to add to the request <code>Action</code> for reply messages. */
    public static final String DEFAULT_OUTPUT_ACTION_SUFFIX = "Response";

    /** The defaults suffix to add to response <code>Action</code> for reply messages. */
    public static final String DEFAULT_FAULT_ACTION_SUFFIX = "Fault";

    // keys are action URIs, values are endpoints
    private final Map<URI, Object> endpointMap = new HashMap<URI, Object>();

    private String outputActionSuffix = DEFAULT_OUTPUT_ACTION_SUFFIX;

    private String faultActionSuffix = DEFAULT_OUTPUT_ACTION_SUFFIX;

    private ApplicationContext applicationContext;

    /** Returns the suffix to add to request <code>Action</code>s for reply messages. */
    public String getOutputActionSuffix() {
        return outputActionSuffix;
    }

    /**
     * Sets the suffix to add to request <code>Action</code>s for reply messages.
     *
     * @see #DEFAULT_OUTPUT_ACTION_SUFFIX
     */
    public void setOutputActionSuffix(String outputActionSuffix) {
        Assert.hasText(outputActionSuffix, "'outputActionSuffix' must not be empty");
        this.outputActionSuffix = outputActionSuffix;
    }

    /** Returns the suffix to add to request <code>Action</code>s for reply fault messages. */
    public String getFaultActionSuffix() {
        return faultActionSuffix;
    }

    /**
     * Sets the suffix to add to request <code>Action</code>s for reply fault messages.
     *
     * @see #DEFAULT_FAULT_ACTION_SUFFIX
     */
    public void setFaultActionSuffix(String faultActionSuffix) {
        Assert.hasText(faultActionSuffix, "'faultActionSuffix' must not be empty");
        this.faultActionSuffix = faultActionSuffix;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected final Object getEndpointInternal(MessageAddressingProperties map) {
        URI action = map.getAction();
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up endpoint for action [" + action + "]");
        }
        Object endpoint = lookupEndpoint(action);
        if (endpoint != null) {
            URI endpointAddress = getEndpointAddress(endpoint);
            if (endpointAddress == null || endpointAddress.equals(map.getTo())) {
                return endpoint;
            }
        }
        return null;
    }

    /**
     * Returns the address property of the given endpoint. The value of this property should match the {@link
     * MessageAddressingProperties#getTo() destination} of incoming messages. May return <code>null</code>  to ignore
     * the destination.
     *
     * @param endpoint the endpoint to return the address for
     * @return the endpoint address; or <code>null</code> to ignore the destination property
     */
    protected abstract URI getEndpointAddress(Object endpoint);

    /**
     * Looks up an endpoint instance for the given action. All keys are tried in order.
     *
     * @param action the action URI
     * @return the associated endpoint instance, or <code>null</code> if not found
     */
    protected Object lookupEndpoint(URI action) {
        return endpointMap.get(action);
    }

    /**
     * Register the specified endpoint for the given action URI.
     *
     * @param action   the action the bean should be mapped to
     * @param endpoint the endpoint instance or endpoint bean name String (a bean name will automatically be resolved
     *                 into the corresponding endpoint bean)
     * @throws org.springframework.beans.BeansException
     *                               if the endpoint couldn't be registered
     * @throws IllegalStateException if there is a conflicting endpoint registered
     */
    protected void registerEndpoint(URI action, Object endpoint) throws BeansException, IllegalStateException {
        Assert.notNull(action, "Action must not be null");
        Assert.notNull(endpoint, "Endpoint object must not be null");
        Object resolvedEndpoint = endpoint;

        if (endpoint instanceof String) {
            String endpointName = (String) endpoint;
            if (applicationContext.isSingleton(endpointName)) {
                resolvedEndpoint = applicationContext.getBean(endpointName);
            }
        }
        Object mappedEndpoint = this.endpointMap.get(action);
        if (mappedEndpoint != null) {
            if (mappedEndpoint != resolvedEndpoint) {
                throw new IllegalStateException("Cannot map endpoint [" + endpoint + "] to action [" + action +
                        "]: There is already endpoint [" + resolvedEndpoint + "] mapped.");
            }
        }
        else {
            this.endpointMap.put(action, resolvedEndpoint);
            if (logger.isDebugEnabled()) {
                logger.debug("Mapped Action [" + action + "] onto endpoint [" + resolvedEndpoint + "]");
            }
        }
    }

    @Override
    protected URI getResponseAction(Object endpoint, MessageAddressingProperties requestMap) {
        URI requestAction = requestMap.getAction();
        if (requestAction != null) {
            return URI.create(requestAction.toString() + getOutputActionSuffix());
        }
        else {
            return null;
        }
    }

    @Override
    protected URI getFaultAction(Object endpoint, MessageAddressingProperties requestMap) {
        URI requestAction = requestMap.getAction();
        if (requestAction != null) {
            return URI.create(requestAction.toString() + getFaultActionSuffix());
        }
        else {
            return null;
        }
    }

}
