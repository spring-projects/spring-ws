/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.client.core;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the interface for objects than can resolve fault {@link WebServiceMessage}s.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public interface FaultMessageResolver {

	/**
	 * Try to resolve the given fault message that got received.
	 * @param message the fault message
	 */
	void resolveFault(WebServiceMessage message) throws IOException;

}
