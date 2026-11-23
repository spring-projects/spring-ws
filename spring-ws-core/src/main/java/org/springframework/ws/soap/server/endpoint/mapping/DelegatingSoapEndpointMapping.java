/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server.endpoint.mapping;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.SoapEndpointMapping;

/**
 * {@code EndpointMapping} implement that adds SOAP actors or roles to a delegate
 * endpoint. Delegates to another {@code EndpointMapping}, set by {@code delegate}, and
 * adds the actors or roles specified by {@code actorsOrRoles}.
 * <p>
 * This endpoint mapping makes it possible to set actors/roles on a specific endpoint,
 * without making the all endpoint mappings depend on SOAP-specific functionality. For
 * normal use, setting an actor or role on an endpoint is not required, the default 'next'
 * role is sufficient.
 * <p>
 * It is only in a scenario when a certain endpoint act as a SOAP intermediary for another
 * endpoint, as described in the SOAP specificication, this mapping is useful.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see org.springframework.ws.soap.SoapHeader#examineMustUnderstandHeaderElements(String)
 * @see org.springframework.ws.soap.SoapVersion#getNextActorOrRoleUri()
 */
public class DelegatingSoapEndpointMapping implements InitializingBean, SoapEndpointMapping {

	private @Nullable EndpointMapping delegate;

	private String @Nullable [] actorsOrRoles;

	private boolean isUltimateReceiver = true;

	/** Sets the delegate {@code EndpointMapping} to resolve the endpoint with. */
	public void setDelegate(EndpointMapping delegate) {
		this.delegate = delegate;
	}

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
	 * Creates a new {@code SoapEndpointInvocationChain} based on the delegate endpoint,
	 * the delegate interceptors, and set actors/roles.
	 * @see #setActorsOrRoles(String[])
	 */
	@Override
	public @Nullable EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception {
		Assert.notNull(this.delegate, "delegate must not be null");
		EndpointInvocationChain delegateChain = this.delegate.getEndpoint(messageContext);
		if (delegateChain != null) {
			return new SoapEndpointInvocationChain(delegateChain.getEndpoint(), delegateChain.getInterceptors(),
					this.actorsOrRoles, this.isUltimateReceiver);
		}
		else {
			return null;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.delegate, "delegate is required");
	}

}
