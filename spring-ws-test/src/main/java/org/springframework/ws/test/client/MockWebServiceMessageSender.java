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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Mock implementation of {@link WebServiceMessageSender}. Contains a list of
 * {@link RequestExpectation}s, and matches each request first against the expectations
 * that already met a request and can meet more of them, then against the next expectation
 * in the order in which they were recorded.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @author Greg Turnquist
 * @author Sharang Gupta
 * @since 2.0
 */
public class MockWebServiceMessageSender implements WebServiceMessageSender {

	private final List<RequestExpectation> expectations = new ArrayList<>();

	private final Set<RequestExpectation> repeatExpectations = new LinkedHashSet<>();

	private @Nullable Iterator<RequestExpectation> expectationIterator;

	@Override
	public synchronized MockSenderConnection createConnection(URI uri) throws IOException {
		Assert.notNull(uri, "'uri' must not be null");
		if (!expectationIterator().hasNext() && this.repeatExpectations.isEmpty()) {
			throw new AssertionError("No further connections expected");
		}
		return new MockSenderConnection(uri, this);
	}

	/**
	 * Always returns {@code true}.
	 */
	@Override
	public boolean supports(URI uri, UriSource uriSource) {
		return true;
	}

	synchronized RequestExpectation expect(ExpectedCount expectedCount) {
		Assert.state(this.expectationIterator == null,
				"Can not expect another connection, the test is already underway");
		RequestExpectation expectation = new RequestExpectation(expectedCount);
		this.expectations.add(expectation);
		return expectation;
	}

	/**
	 * Count the given request against the expectation that it meets, and return that
	 * expectation.
	 * @param uri the URI of the connection
	 * @param request the request message
	 * @return the expectation that the request meets
	 * @throws IOException in case of I/O errors
	 * @throws AssertionError if the request does not meet the expectations
	 */
	synchronized RequestExpectation matchRequest(URI uri, WebServiceMessage request) throws IOException {
		RequestExpectation expectation = findExpectation(uri, request);
		expectation.incrementAndValidate();
		if (expectation.hasRemainingCount()) {
			this.repeatExpectations.add(expectation);
		}
		else {
			this.repeatExpectations.remove(expectation);
		}
		return expectation;
	}

	/**
	 * Find the expectation that the given request meets: one of the expectations that
	 * already met a request and can meet more of them, or else the next recorded
	 * expectation. If the request meets none of them and there is no next expectation,
	 * the mismatch of the first expectation that can meet more requests is reported.
	 */
	private RequestExpectation findExpectation(URI uri, WebServiceMessage request) throws IOException {
		AssertionError repeatMismatch = null;
		for (RequestExpectation expectation : this.repeatExpectations) {
			try {
				expectation.match(uri, request);
				return expectation;
			}
			catch (AssertionError ex) {
				if (repeatMismatch == null) {
					repeatMismatch = ex;
				}
			}
		}
		Iterator<RequestExpectation> iterator = expectationIterator();
		if (!iterator.hasNext()) {
			throw (repeatMismatch != null) ? repeatMismatch : new AssertionError("No further connections expected");
		}
		RequestExpectation expectation = iterator.next();
		expectation.match(uri, request);
		return expectation;
	}

	private Iterator<RequestExpectation> expectationIterator() {
		if (this.expectationIterator == null) {
			this.expectationIterator = this.expectations.iterator();
		}
		return this.expectationIterator;
	}

	synchronized void verifyConnections() {
		if (this.expectations.stream().anyMatch((expectation) -> !expectation.isSatisfied())) {
			throw new AssertionError("Further connection(s) expected");
		}
	}

	synchronized void reset() {
		this.expectations.clear();
		this.repeatExpectations.clear();
		this.expectationIterator = null;
	}

}
