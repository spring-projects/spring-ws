/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.test.client;

import static org.springframework.ws.test.support.AssertionErrors.*;

import java.net.URI;

import org.springframework.ws.WebServiceMessage;

/**
 * Matches {@link URI}s.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
class UriMatcher implements RequestMatcher {

	private final URI expected;

	UriMatcher(URI expected) {
		this.expected = expected;
	}

	@Override
	public void match(URI actual, WebServiceMessage request) {
		assertEquals("Unexpected connection", expected, actual, "Payload", request.getPayloadSource());
	}
}
