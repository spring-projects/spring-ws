/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the contract for matching response messages to expectations. Implementations of
 * this interface are returned by {@link ResponseMatchers}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface ResponseMatcher {

	/**
	 * Matches the given response message against the expectations. Implementations
	 * typically make use of JUnit-based assertions.
	 * @param request the request message
	 * @param response the response message to make assertions on
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	void match(WebServiceMessage request, WebServiceMessage response) throws IOException, AssertionError;

}
