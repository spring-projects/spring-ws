/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.server;

/**
 * Allows for setting up expectation about response messages. Implementations of this
 * interface are returned by {@link MockWebServiceClient#sendRequest(RequestCreator)}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface ResponseActions {

	/**
	 * Sets up an expectation about the response message.
	 * @param responseMatcher the response matcher that defines expectations
	 * @return an instance of {@link ResponseActions}, to set up further expectations
	 */
	ResponseActions andExpect(ResponseMatcher responseMatcher);

}
