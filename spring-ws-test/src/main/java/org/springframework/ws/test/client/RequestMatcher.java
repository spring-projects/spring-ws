/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the contract for matching requests to expectations.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface RequestMatcher {

	/**
	 * Matches the given request message against the expectations. Implementations
	 * typically make use of JUnit-based assertions.
	 * @param uri the uri connected to
	 * @param request the request message to make assertions on
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	void match(URI uri, WebServiceMessage request) throws IOException, AssertionError;

}
