/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws;

/**
 * Exception thrown when an endpoint cannot be resolved for an incoming message request.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public final class NoEndpointFoundException extends WebServiceException {

	public NoEndpointFoundException(WebServiceMessage request) {
		super("No endpoint can be found for request [" + request + "]");
	}

}
