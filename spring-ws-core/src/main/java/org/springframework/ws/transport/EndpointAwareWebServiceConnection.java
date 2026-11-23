/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport;

import org.springframework.ws.NoEndpointFoundException;

/**
 * Sub-interface of {@link WebServiceConnection} that is aware of any server-side
 * situations where an endpoint is not found. Typically, this results in a special error
 * codes.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see NoEndpointFoundException
 */
public interface EndpointAwareWebServiceConnection extends WebServiceConnection {

	/** Called when an endpoint is not found. */
	void endpointNotFound();

}
