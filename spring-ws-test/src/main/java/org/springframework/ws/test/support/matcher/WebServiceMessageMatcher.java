/*
 * Copyright 2005-2022 the original author or authors.
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
	 * Matches the given message against the expectations. Implementations typically make use of JUnit-based assertions.
	 *
	 * @param message the message
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if expectations are not met
	 */
	void match(WebServiceMessage message) throws IOException, AssertionError;
}
