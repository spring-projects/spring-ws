/*
 * Copyright 2005-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.test.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;

/**
 * An expectation of the {@link MockWebServiceServer}: the {@link RequestMatcher request
 * matchers} that a request has to meet, the {@link ResponseCreator} that creates the
 * response, and the {@link ExpectedCount expected number} of matching requests.
 * Implements {@link ResponseActions} to form a fluent API.
 *
 * @author Sharang Gupta
 * @since 5.1
 */
final class RequestExpectation implements ResponseActions {

	private final ExpectedCount expectedCount;

	private final List<RequestMatcher> requestMatchers = new ArrayList<>();

	private @Nullable ResponseCreator responseCreator;

	private int matchedCount;

	RequestExpectation(ExpectedCount expectedCount) {
		Assert.notNull(expectedCount, "'expectedCount' must not be null");
		this.expectedCount = expectedCount;
	}

	@Override
	public ResponseActions andExpect(RequestMatcher requestMatcher) {
		Assert.notNull(requestMatcher, "'requestMatcher' must not be null");
		this.requestMatchers.add(requestMatcher);
		return this;
	}

	@Override
	public void andRespond(ResponseCreator responseCreator) {
		Assert.notNull(responseCreator, "'responseCreator' must not be null");
		this.responseCreator = responseCreator;
	}

	/**
	 * Match the given request against the request matchers of this expectation.
	 * @param uri the URI of the connection
	 * @param request the request message
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if the request does not meet the expectation
	 */
	void match(URI uri, WebServiceMessage request) throws IOException {
		if (this.requestMatchers.isEmpty()) {
			throw new AssertionError("Unexpected send() for [" + request + "]");
		}
		for (RequestMatcher requestMatcher : this.requestMatchers) {
			requestMatcher.match(uri, request);
		}
	}

	/**
	 * Count a request that meets this expectation.
	 * @throws AssertionError if more requests than the maximum count met the expectation
	 */
	void incrementAndValidate() {
		this.matchedCount++;
		if (this.matchedCount > this.expectedCount.getMaxCount()) {
			throw new AssertionError("No further connections expected");
		}
	}

	boolean hasRemainingCount() {
		return this.matchedCount < this.expectedCount.getMaxCount();
	}

	boolean isSatisfied() {
		return this.matchedCount >= this.expectedCount.getMinCount();
	}

	@Nullable ResponseCreator getResponseCreator() {
		return this.responseCreator;
	}

}
