/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.mapping;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.mapping.AbstractMapBasedEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;
import org.springframework.ws.soap.support.SoapUtils;

/**
 * Implementation of the {@code EndpointMapping} interface to map from {@code SOAPAction}
 * headers to endpoint beans. Supports both mapping to bean instances and mapping to bean
 * names: the latter is required for prototype handlers.
 * <p>
 * The {@code endpointMap} property is suitable for populating the endpoint map with bean
 * references, e.g. via the map element in XML bean definitions.
 * <p>
 * Mappings to bean names can be set via the {@code mappings} property, in a form accepted
 * by the {@code java.util.Properties} class, like as follows:
 *
 * <pre>
 * http://www.springframework.org/spring-ws/samples/airline/BookFlight=bookFlightEndpoint
 * http://www.springframework.org/spring-ws/samples/airline/GetFlights=getFlightsEndpoint
 * </pre>
 *
 * The syntax is SOAP_ACTION=ENDPOINT_BEAN_NAME.
 * <p>
 * This endpoint mapping does not read from the request message, and therefore is more
 * suitable for message factories which directly read from the transport request (such as
 * the {@link org.springframework.ws.soap.saaj.SaajSoapMessageFactory} with the
 * {@code payloadCaching} disabled).
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class SoapActionEndpointMapping extends AbstractMapBasedEndpointMapping implements SoapEndpointMapping {

	private String @Nullable [] actorsOrRoles;

	private boolean isUltimateReceiver = true;

	@Override
	public final void setActorOrRole(String actorOrRole) {
		Assert.notNull(actorOrRole, "actorOrRole must not be null");
		this.actorsOrRoles = new String[] { actorOrRole };
	}

	@Override
	public final void setActorsOrRoles(String[] actorsOrRoles) {
		Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
		this.actorsOrRoles = actorsOrRoles;
	}

	@Override
	public final void setUltimateReceiver(boolean ultimateReceiver) {
		this.isUltimateReceiver = ultimateReceiver;
	}

	/**
	 * Creates a new {@code SoapEndpointInvocationChain} based on the given endpoint, and
	 * the set interceptors, and actors/roles.
	 * @param endpoint the endpoint
	 * @param interceptors the endpoint interceptors
	 * @return the created invocation chain
	 * @see #setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
	 * @see #setActorsOrRoles(String[])
	 */
	@Override
	protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext,
			Object endpoint, EndpointInterceptor[] interceptors) {
		return new SoapEndpointInvocationChain(endpoint, interceptors, this.actorsOrRoles, this.isUltimateReceiver);
	}

	@Override
	protected @Nullable String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
		if (messageContext.getRequest() instanceof SoapMessage request) {
			return SoapUtils.unescapeAction(request.getSoapAction());
		}
		else {
			return null;
		}
	}

	@Override
	protected boolean validateLookupKey(String key) {
		return StringUtils.hasLength(key);
	}

}
