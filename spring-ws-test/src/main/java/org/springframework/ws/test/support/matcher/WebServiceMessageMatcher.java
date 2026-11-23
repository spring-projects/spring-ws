/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.support.matcher;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the general contract for matching messages to expectations.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface WebServiceMessageMatcher {

	/**
	 * Matches the given message against the expectations. Implementations typically make
	 * use of JUnit-based assertions.
	 * @param message the message
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	void match(WebServiceMessage message) throws IOException, AssertionError;

}
