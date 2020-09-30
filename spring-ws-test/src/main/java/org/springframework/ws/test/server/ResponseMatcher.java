/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.server;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the contract for matching response messages to expectations. Implementations of this interface are returned
 * by {@link ResponseMatchers}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public interface ResponseMatcher {

	/**
	 * Matches the given response message against the expectations. Implementations typically make use of JUnit-based
	 * assertions.
	 *
	 * @param request the request message
	 * @param response the response message to make assertions on
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	void match(WebServiceMessage request, WebServiceMessage response) throws IOException, AssertionError;

}
