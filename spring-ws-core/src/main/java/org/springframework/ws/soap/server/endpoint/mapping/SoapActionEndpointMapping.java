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

package org.springframework.ws.soap.server.endpoint.mapping;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.mapping.AbstractMapBasedEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;

/**
 * Implementation of the {@code EndpointMapping} interface to map from {@code SOAPAction} headers to endpoint beans.
 * Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype handlers.
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
 * The syntax is SOAP_ACTION=ENDPOINT_BEAN_NAME.
 * <p>
 * This endpoint mapping does not read from the request message, and therefore is more suitable for message factories
 * which directly read from the transport request (such as the {@link AxiomSoapMessageFactory} with the
 * {@code payloadCaching} disabled).
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapActionEndpointMapping extends AbstractMapBasedEndpointMapping implements SoapEndpointMapping {

	private String[] actorsOrRoles;

	private boolean isUltimateReceiver = true;

	@Override
	public final void setActorOrRole(String actorOrRole) {
		Assert.notNull(actorOrRole, "actorOrRole must not be null");
		actorsOrRoles = new String[] { actorOrRole };
	}

	@Override
	public final void setActorsOrRoles(String[] actorsOrRoles) {
		Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
		this.actorsOrRoles = actorsOrRoles;
	}

	@Override
	public final void setUltimateReceiver(boolean ultimateReceiver) {
		isUltimateReceiver = ultimateReceiver;
	}

	/**
	 * Creates a new {@code SoapEndpointInvocationChain} based on the given endpoint, and the set interceptors, and
	 * actors/roles.
	 *
	 * @param endpoint the endpoint
	 * @param interceptors the endpoint interceptors
	 * @return the created invocation chain
	 * @see #setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
	 * @see #setActorsOrRoles(String[])
	 */
	@Override
	protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext, Object endpoint,
			EndpointInterceptor[] interceptors) {
		return new SoapEndpointInvocationChain(endpoint, interceptors, actorsOrRoles, isUltimateReceiver);
	}

	@Override
	protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		if (messageContext.getRequest() instanceof SoapMessage) {
			SoapMessage request = (SoapMessage) messageContext.getRequest();
			String soapAction = request.getSoapAction();
			if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"'
					&& soapAction.charAt(soapAction.length() - 1) == '"') {
				return soapAction.substring(1, soapAction.length() - 1);
			} else {
				return soapAction;
			}
		} else {
			return null;
		}
	}

	@Override
	protected boolean validateLookupKey(String key) {
		return StringUtils.hasLength(key);
	}
}
