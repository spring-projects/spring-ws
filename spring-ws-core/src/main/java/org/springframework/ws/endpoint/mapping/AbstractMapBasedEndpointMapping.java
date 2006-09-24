/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.endpoint.mapping;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;

/**
 * Abstract base class for endpoint mapping that are based on a <code>Map</code>. Provides mappings of application
 * context beans as well as a settable map.
 * <p/>
 * Subclasses determine the exact nature of the key in the enpoint map; this can be a qualified name, a SOAP Header, the
 * result of a XPath validation. The values are always endpoint objects, or bean names of endpoint objects.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractMapBasedEndpointMapping extends AbstractEndpointMapping {

    private boolean lazyInitEndpoints = false;

    private boolean registerBeanNames = false;

    private final Map endpointMap = new HashMap();

    // holds mappings set via setEndpointMap and setMappings
    private Map temporaryEndpointMap = new HashMap();

    /**
     * Set whether to lazily initialize endpoints. Only applicable to singleton endpoints, as prototypes are always
     * lazily initialized. Default is <code>false</code>, as eager initialization allows for more efficiency through
     * referencing the controller objects directly.
     * <p/>
     * If you want to allow your endpoints to be lazily initialized, make them "lazy-init" and set this flag to
     * <code>true</code>. Just making them "lazy-init" will not work, as they are initialized through the references
     * from the endpoint mapping in this case.
     */
    public void setLazyInitEndpoints(boolean lazyInitEndpoints) {
        this.lazyInitEndpoints = lazyInitEndpoints;
    }

    /**
     * Set whether to register bean names found in the application context. Setting this to <code>true</code> will
     * register all beans found in the application context under their name. Default is <code>false</code>.
     */
    public final void setRegisterBeanNames(boolean registerBeanNames) {
        this.registerBeanNames = registerBeanNames;
    }

    /**
     * Sets a Map with keys and endpoint beans as values. The nature of the keys in the given map depends on the exact
     * subclass used. They can be qualified names, for instance, or mime headers.
     *
     * @throws IllegalArgumentException if the endpoint is invalid
     */
    public final void setEndpointMap(Map endpointMap) {
        temporaryEndpointMap.putAll(endpointMap);
    }

    /**
     * Maps keys to endpoint bean names. The nature of the property names depends on the exact subclass used. They can
     * be qualified names, for instance, or mime headers.
     */
    public void setMappings(Properties mappings) {
        temporaryEndpointMap.putAll(mappings);
    }

    /**
     * Validates the given endpoint key. Should return <code>true</code> is the given string is valid.
     */
    protected abstract boolean validateLookupKey(String key);

    /**
     * Returns the the endpoint keys for the given message context.
     *
     * @return the registration keys
     */
    protected abstract String getLookupKeyForMessage(MessageContext messageContext) throws Exception;

    /**
     * Lookup an endpoint for the given message. The extraction of the endpoint key is delegated to the concrete
     * subclass.
     *
     * @return the looked up endpoint, or <code>null</code>
     */
    protected Object getEndpointInternal(MessageContext messageContext) throws Exception {
        String key = getLookupKeyForMessage(messageContext);
        if (!StringUtils.hasLength(key)) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up endpoint for [" + key + "]");
        }
        return lookupEndpoint(key);
    }

    /**
     * Looks up an endpoint instance for the given keys. All keys are tried in order.
     *
     * @param key key the beans are mapped to
     * @return the associated endpoint instance, or <code>null</code> if not found
     */
    protected Object lookupEndpoint(String key) {
        return endpointMap.get(key);
    }

    /**
     * Register the given endpoint instance under the registration key.
     *
     * @param key      the string representation of the registration key
     * @param endpoint the endpoint instance
     * @throws org.springframework.beans.BeansException
     *          if the endpoint could not be registered
     */
    protected void registerEndpoint(String key, Object endpoint) throws BeansException {
        Object mappedEndpoint = endpointMap.get(key);
        if (mappedEndpoint != null) {
            throw new ApplicationContextException("Cannot map endpoint [" + endpoint + "] on registration key [" + key +
                    "]: there's already endpoint [" + mappedEndpoint + "] mapped");
        }
        if (!lazyInitEndpoints && endpoint instanceof String) {
            String endpointName = (String) endpoint;
            endpoint = resolveStringEndpoint(endpointName);
        }
        if (endpoint == null) {
            throw new ApplicationContextException("Could not find endpoint for key [" + key + "]");
        }
        endpointMap.put(key, endpoint);
        if (logger.isDebugEnabled()) {
            logger.debug("Mapped key [" + key + "] onto endpoint [" + endpoint + "]");
        }
    }

    /**
     * Registers annd checks the set endpoints. Checks the beans set through <code>setEndpointMap</code> and
     * <code>setMappings</code>, and registers the bean names found in the application context, if
     * <code>registerBeanNames</code> is set to <code>true</code>.
     *
     * @throws ApplicationContextException if either of the endpoints defined via <code>setEndpointMap</code> or
     *                                     <code>setMappings</code> is invalid
     * @see #setEndpointMap(java.util.Map)
     * @see #setMappings(java.util.Properties)
     * @see #setRegisterBeanNames(boolean)
     */
    protected final void initApplicationContext() throws BeansException {
        for (Iterator iter = temporaryEndpointMap.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Object endpoint = temporaryEndpointMap.get(key);
            if (!validateLookupKey(key)) {
                throw new ApplicationContextException("Invalid key [" + key + "] for endpoint [" + endpoint + "]");
            }
            registerEndpoint(key, endpoint);
        }
        temporaryEndpointMap = null;
        if (registerBeanNames) {
            if (logger.isDebugEnabled()) {
                logger.debug("Looking for endpoint mappings in application context: [" + getApplicationContext() + "]");
            }
            String[] beanNames = getApplicationContext().getBeanDefinitionNames();
            for (int i = 0; i < beanNames.length; i++) {
                if (validateLookupKey(beanNames[i])) {
                    registerEndpoint(beanNames[i], beanNames[i]);
                }
                String[] aliases = getApplicationContext().getAliases(beanNames[i]);
                for (int j = 0; j < aliases.length; j++) {
                    if (validateLookupKey(aliases[j])) {
                        registerEndpoint(aliases[j], beanNames[i]);
                    }
                }
            }
        }
    }

}
