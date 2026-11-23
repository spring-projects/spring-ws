/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceFaultException;

/**
 * Simple fault resolver that simply throws a {@link WebServiceFaultException} when a
 * fault occurs.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see WebServiceFaultException
 */
public class SimpleFaultMessageResolver implements FaultMessageResolver {

	/** Throws a new {@code WebServiceFaultException}. */
	@Override
	public void resolveFault(WebServiceMessage message) {
		if (message instanceof FaultAwareWebServiceMessage) {
			throw new WebServiceFaultException((FaultAwareWebServiceMessage) message);
		}
		else {
			throw new WebServiceFaultException("Message has unknown fault: " + message);
		}
	}

}
