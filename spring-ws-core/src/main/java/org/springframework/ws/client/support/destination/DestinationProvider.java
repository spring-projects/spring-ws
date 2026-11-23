/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.destination;

import java.net.URI;

import org.jspecify.annotations.Nullable;

/**
 * Strategy interface for providing a
 * {@link org.springframework.ws.client.core.WebServiceTemplate} destination URI at
 * runtime.
 * <p>
 * Typically implemented by providers that use WSDL, a UDDI registry, or some other form
 * to determine the destination URI.
 *
 * @author Arjen Poutsma
 * @since 1.5.4
 * @see org.springframework.ws.client.core.WebServiceTemplate#setDestinationProvider(DestinationProvider)
 */
public interface DestinationProvider {

	/**
	 * Return the destination URI.
	 * @return the destination URI
	 */
	@Nullable URI getDestination();

}
