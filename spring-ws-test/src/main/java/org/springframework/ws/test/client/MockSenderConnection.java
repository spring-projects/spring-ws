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
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Mock implementation of {@link WebServiceConnection}. Implements {@link ResponseActions}
 * to form a fluent API.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @since 2.0
 */
class MockSenderConnection implements WebServiceConnection, ResponseActions {

	private final List<RequestMatcher> requestMatchers = new LinkedList<>();

	private URI uri;

	private WebServiceMessage request;

	private ResponseCreator responseCreator;

	void addRequestMatcher(RequestMatcher requestMatcher) {
		Assert.notNull(requestMatcher, "'requestMatcher' must not be null");
		this.requestMatchers.add(requestMatcher);
	}

	void setUri(URI uri) {
		Assert.notNull(uri, "'uri' must not be null");
		this.uri = uri;
	}

	// ResponseActions implementation

	@Override
	public ResponseActions andExpect(RequestMatcher requestMatcher) {
		addRequestMatcher(requestMatcher);
		return this;
	}

	@Override
	public void andRespond(ResponseCreator responseCreator) {
		Assert.notNull(responseCreator, "'responseCreator' must not be null");
		this.responseCreator = responseCreator;
	}

	// FaultAwareWebServiceConnection implementation

	@Override
	public void send(WebServiceMessage message) throws IOException {
		if (!this.requestMatchers.isEmpty()) {
			for (RequestMatcher requestMatcher : this.requestMatchers) {
				requestMatcher.match(this.uri, message);
			}
		}
		else {
			throw new AssertionError("Unexpected send() for [" + message + "]");
		}
		this.request = message;
	}

	@Override
	public WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException {
		if (this.responseCreator != null) {
			return this.responseCreator.createResponse(this.uri, this.request, messageFactory);
		}
		else {
			return null;
		}
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public boolean hasError() throws IOException {
		return this.responseCreator instanceof ErrorResponseCreator;
	}

	@Override
	public String getErrorMessage() throws IOException {
		if (this.responseCreator instanceof ErrorResponseCreator) {
			return ((ErrorResponseCreator) this.responseCreator).getErrorMessage();
		}
		else {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		this.requestMatchers.clear();
		this.request = null;
		this.responseCreator = null;
		this.uri = null;
	}

}
