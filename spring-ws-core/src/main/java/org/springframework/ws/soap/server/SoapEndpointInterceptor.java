/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.soap.server;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;

/**
 * SOAP-specific extension of the {@link EndpointInterceptor} interface. Allows for
 * handling of SOAP faults, which are considered different from regular responses.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface SoapEndpointInterceptor extends EndpointInterceptor {

	/**
	 * Given a {@link SoapHeaderElement}, return whether or not this
	 * {@link SoapEndpointInterceptor} understands it.
	 * @param header the header
	 * @return {@code true} if understood, {@code false} otherwise
	 */
	boolean understands(SoapHeaderElement header);

}
