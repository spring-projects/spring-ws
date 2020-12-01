/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;

/**
 * Implementation of the {@code EndpointMapping} interface to map from WS-Addressing {@code Action} Message Addressing
 * Property to endpoint beans. Supports both mapping to bean instances and mapping to bean names.
 * <p>
 * The {@code endpointMap} property is suitable for populating the endpoint map with bean references, e.g. via the map
 * element in XML bean definitions.
 * <p>
 * Mappings to bean names can be set via the {@code mappings} property, in a form accepted by the
 * {@code java.util.Properties} class, like as follows:
 * 
 * <pre>
 * http://www.springframework.org/spring-ws/samples/airline/BookFlight=bookFlightEndpoint
 * http://www.springframework.org/spring-ws/samples/airline/GetFlights=getFlightsEndpoint
 * </pre>
 * 
 * The syntax is WS_ADDRESSING_ACTION=ENDPOINT_BEAN_NAME.
 * <p>
 * If set, the {@link #setAddress(URI) address} property should be equal to the
 * {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getTo() destination} property of the
 * incominging message. As such, it can be used to create multiple Endpoint References, by defining multiple
 * {@code SimpleActionEndpointMapping} bean definitions with different {@code address} property values.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getAction()
 * @since 1.5.0
 */
public class SimpleActionEndpointMapping extends AbstractActionEndpointMapping {

	// contents will be copied over to endpointMap
	private final Map<URI, Object> actionMap = new HashMap<URI, Object>();

	private URI address;

	/**
	 * Map action URIs to endpoint bean names. This is the typical way of configuring this EndpointMapping.
	 *
	 * @param mappings properties with URLs as keys and bean names as values
	 * @see #setActionMap(java.util.Map)
	 */
	public void setMappings(Properties mappings) throws URISyntaxException {
		setActionMap(mappings);
	}

	/**
	 * Set a Map with action URIs as keys and handler beans (or handler bean names) as values. Convenient for population
	 * with bean references.
	 *
	 * @param actionMap map with action URIs as keys and beans as values
	 * @see #setMappings
	 */
	public void setActionMap(Map<?, Object> actionMap) throws URISyntaxException {
		for (Map.Entry<?, Object> entry : actionMap.entrySet()) {
			URI action;
			if (entry.getKey() instanceof String) {
				action = new URI((String) entry.getKey());
			} else if (entry.getKey() instanceof URI) {
				action = (URI) entry.getKey();
			} else {
				throw new IllegalArgumentException("Invalid key [" + entry.getKey() + "]; expected String or URI");
			}
			this.actionMap.put(action, entry.getValue());
		}
	}

	/**
	 * Set the address property. If set, value of this property is compared to the
	 * {@link org.springframework.ws.soap.addressing.core.MessageAddressingProperties#getTo() destination} property of the
	 * incominging message.
	 *
	 * @param address the address URI
	 */
	public void setAddress(URI address) {
		this.address = address;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		registerEndpoints(actionMap);
	}

	/**
	 * Register all endpoints specified in the action map.
	 *
	 * @param actionMap Map with action URIs as keys and endppint beans or bean names as values
	 * @throws BeansException if an endpoint couldn't be registered
	 * @throws IllegalStateException if there is a conflicting endpoint registered
	 */
	protected void registerEndpoints(Map<URI, Object> actionMap) throws BeansException {
		if (actionMap.isEmpty()) {
			logger.warn("Neither 'actionMap' nor 'mappings' set on SimpleActionEndpointMapping");
		} else {
			for (Map.Entry<URI, Object> entry : actionMap.entrySet()) {
				URI action = entry.getKey();
				Object endpoint = entry.getValue();
				// Remove whitespace from endpoint bean name.
				if (endpoint instanceof String) {
					endpoint = ((String) endpoint).trim();
				}
				registerEndpoint(action, endpoint);
			}
		}
	}

	@Override
	protected URI getEndpointAddress(Object endpoint) {
		return address;
	}
}
