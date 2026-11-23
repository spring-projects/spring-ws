/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

/**
 * Allows for setting up responses and additional expectations. Implementations of this
 * interface are returned by {@link MockWebServiceServer#expect(RequestMatcher)}.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
public interface ResponseActions {

	/**
	 * Allows for further expectations to be set on the request.
	 * @return the request expectations
	 */
	ResponseActions andExpect(RequestMatcher requestMatcher);

	/**
	 * Sets the {@link ResponseCreator} for this mock.
	 * @param responseCreator the response creator
	 */
	void andRespond(ResponseCreator responseCreator);

}
