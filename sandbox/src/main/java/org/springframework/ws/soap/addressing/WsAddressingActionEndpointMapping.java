/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.soap.addressing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

/**
 * Implementation of the <code>EndpointMapping</code> interface to map from WS-Addressing <code>Action</code> headers to
 * endpoint beans. Supports both mapping to bean instances and mapping to bean names.
 * <p/>
 * The <code>endpointMap</code> property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 * <p/>
 * Mappings to bean names can be set via the <code>mappings</code> property, in a form accepted by the
 * <code>java.util.Properties</code> class, like as follows:
 * <pre>
 * http://www.springframework.org/spring-ws/samples/airline/BookFlight=bookFlightEndpoint
 * http://www.springframework.org/spring-ws/samples/airline/GetFlights=getFlightsEndpoint
 * </pre>
 * The syntax is WS_ADDRESSING_ACTION=ENDPOINT_BEAN_NAME.
 * <p/>
 * If set, the <code>destination</code> property is used suitable for further endpoint determination. can be used to set
 * an EndpointReference destination (i.e. the <code>To</code> element). If this propert
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class WsAddressingActionEndpointMapping extends AbstractWsAddressingEndpointMapping
        implements ApplicationContextAware {

    // keys are action URIs, values are endpoints
    private final Map endpointMap = new HashMap();

    // contents will be copied over to endpointMap
    private final Map temporaryEndpointMap = new HashMap();

    private URI destination;

    private ApplicationContext applicationContext;

    public void setMappings(Properties mappings) throws URISyntaxException {
        setEndpointMap(mappings);
    }

    public void setEndpointMap(Map actionMap) throws URISyntaxException {
        Iterator it = actionMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            URI action;
            if (entry.getKey() instanceof String) {
                action = new URI((String) entry.getKey());
            }
            else if (entry.getKey() instanceof URI) {
                action = (URI) entry.getKey();
            }
            else {
                throw new IllegalArgumentException("Invalid key [" + entry.getKey() + "]; expected String or URI");
            }
            this.temporaryEndpointMap.put(action, entry.getValue());
        }
    }

    public void setDestination(URI destination) {
        this.destination = destination;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        registerEndpoints(temporaryEndpointMap);
    }

    protected Object getEndpointInternal(MessageAddressingProperties map) {
        // MAP address much match the defined EPR address
        if (destination != null && !destination.equals(map.getTo())) {
            return null;
        }
        return endpointMap.get(map.getAction());
    }

    protected void registerEndpoints(Map endpointMap) throws BeansException {
        if (endpointMap.isEmpty()) {
            logger.warn("Neither 'actionMap' nor 'mappings' set on WsAddressingActionEndpointMapping");
        }
        else {
            Iterator it = endpointMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                URI action = (URI) entry.getKey();
                Object endpoint = entry.getValue();
                // Remove whitespace from endpoint bean name.
                if (endpoint instanceof String) {
                    endpoint = ((String) endpoint).trim();
                }
                registerEndpoint(action, endpoint);
            }
        }
    }

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
}
