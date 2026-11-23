/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;

/**
 * SOAP-specific subclass of the {@code EndpointInvocationChain}. Adds associated actors
 * (SOAP 1.1) or roles (SOAP 1.2). Used by the {@code SoapMessageDispatcher} to determine
 * the MustUnderstand headers for particular endpoint.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #getActorsOrRoles()
 * @see SoapMessageDispatcher
 */
public class SoapEndpointInvocationChain extends EndpointInvocationChain {

	private String @Nullable [] actorsOrRoles;

	private boolean isUltimateReceiver = true;

	/**
	 * Create new {@code SoapEndpointInvocationChain}.
	 * @param endpoint the endpoint object to invoke
	 */
	public SoapEndpointInvocationChain(Object endpoint) {
		super(endpoint);
	}

	/**
	 * Create new {@code SoapEndpointInvocationChain}.
	 * @param endpoint the endpoint object to invoke
	 * @param interceptors the array of interceptors to apply
	 */
	public SoapEndpointInvocationChain(Object endpoint, EndpointInterceptor @Nullable [] interceptors) {
		super(endpoint, interceptors);
	}

	/**
	 * Create new {@code EndpointInvocationChain}.
	 * @param endpoint the endpoint object to invoke
	 * @param interceptors the array of interceptors to apply
	 * @param actorsOrRoles the array of actorsOrRoles to set
	 * @param isUltimateReceiver whether this chain fullfils the SOAP 1.2 Ultimate
	 * receiver role
	 */
	public SoapEndpointInvocationChain(Object endpoint, EndpointInterceptor @Nullable [] interceptors,
			String @Nullable [] actorsOrRoles, boolean isUltimateReceiver) {
		super(endpoint, interceptors);
		this.actorsOrRoles = actorsOrRoles;
		this.isUltimateReceiver = isUltimateReceiver;
	}

	/**
	 * Gets the actors (SOAP 1.1) or roles (SOAP 1.2) associated with an invocation of
	 * this chain and its contained interceptors and endpoint.
	 * @return a string array of URIs for SOAP actors/roles
	 */
	public String @Nullable [] getActorsOrRoles() {
		return this.actorsOrRoles;
	}

	/**
	 * Indicates whether this chain fulfills the SOAP 1.2 Ultimate Receiver role. Default
	 * is {@code true}.
	 */
	public boolean isUltimateReceiver() {
		return this.isUltimateReceiver;
	}

}
