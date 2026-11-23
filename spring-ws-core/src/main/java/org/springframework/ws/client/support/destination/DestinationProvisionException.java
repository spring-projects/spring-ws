/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.support.destination;

import org.springframework.ws.client.WebServiceClientException;

/**
 * Thrown by a {@link DestinationProvider} when it cannot provide a destination.
 *
 * @author Arjen Poutsma
 * @since 1.5.4
 */
@SuppressWarnings("serial")
public class DestinationProvisionException extends WebServiceClientException {

	public DestinationProvisionException(String msg) {
		super(msg);
	}

	public DestinationProvisionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
