/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server;

import org.springframework.ws.server.EndpointMapping;

/**
 * SOAP-specific sub-interface of the {@code EndpointMapping}. Adds associated actors
 * (SOAP 1.1) or roles (SOAP 1.2). Used by the {@code SoapMessageDispatcher} to determine
 * the MustUnderstand headers for particular endpoint.
 * <p>
 * The main purpose for this interface is to add consitency between all SOAP-specific
 * {@code EndpointMappings}. The {@code SoapMessageDispatcher} does not require all
 * endpoint mappings to implement this interface.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapEndpointMapping extends EndpointMapping {

	/**
	 * Sets a single SOAP actor/actorOrRole to apply to all endpoints mapped by the
	 * delegate endpoint mapping.
	 */
	void setActorOrRole(String actorOrRole);

	/**
	 * Sets the array of SOAP actors/actorsOrRoles to apply to all endpoints mapped by the
	 * delegate endpoint mapping.
	 */
	void setActorsOrRoles(String[] actorsOrRoles);

	/**
	 * Indicates whether this the endpoint fulfills the SOAP 1.2 Ultimate Receiver role.
	 */
	void setUltimateReceiver(boolean ultimateReceiver);

}
