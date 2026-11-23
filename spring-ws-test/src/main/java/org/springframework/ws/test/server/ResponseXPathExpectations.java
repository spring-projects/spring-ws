/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

/**
 * Allows for setting up expectations on XPath expressions.
 * <p>
 * Implementations of this interface are returned by
 * {@link org.springframework.ws.test.client.RequestMatchers#xpath(String)} and
 * {@link org.springframework.ws.test.client.RequestMatchers#xpath(String, java.util.Map)},
 * as part of the fluent API. As such, it is not typical to implement this interface
 * yourself.
 *
 * @author Lukas Krecan
 * @author Arjen Poutsma
 * @since 2.0
 * @see org.springframework.ws.test.client.RequestMatchers#xpath(String)
 * @see org.springframework.ws.test.client.RequestMatchers#xpath(String, java.util.Map)
 */
public interface ResponseXPathExpectations {

	/**
	 * Expects the XPath expression to exist.
	 * @return the request matcher
	 */
	ResponseMatcher exists();

	/**
	 * Expects the XPath expression to not exist.
	 * @return the request matcher
	 */
	ResponseMatcher doesNotExist();

	/**
	 * Expects the XPath expression to evaluate to the given boolean.
	 * @param expectedValue the expected value
	 * @return the request matcher
	 */
	ResponseMatcher evaluatesTo(boolean expectedValue);

	/**
	 * Expects the XPath expression to evaluate to the given integer.
	 * @param expectedValue the expected value
	 * @return the request matcher
	 */
	ResponseMatcher evaluatesTo(int expectedValue);

	/**
	 * Expects the XPath expression to evaluate to the given double.
	 * @param expectedValue the expected value
	 * @return the request matcher
	 */
	ResponseMatcher evaluatesTo(double expectedValue);

	/**
	 * Expects the XPath expression to evaluate to the given string.
	 * @param expectedValue the expected value
	 * @return the request matcher
	 */
	ResponseMatcher evaluatesTo(String expectedValue);

}
