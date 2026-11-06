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
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;

/**
 * Mock implementation of {@link WebServiceConnection}. A connection is created for each
 * request, and finds the {@link RequestExpectation} that the request meets when it is
 * sent.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @author Sharang Gupta
 * @since 2.0
 */
class MockSenderConnection implements WebServiceConnection {

	private final URI uri;

	private final MockWebServiceMessageSender messageSender;

	private @Nullable WebServiceMessage request;

	private @Nullable RequestExpectation expectation;

	MockSenderConnection(URI uri, MockWebServiceMessageSender messageSender) {
		Assert.notNull(uri, "'uri' must not be null");
		Assert.notNull(messageSender, "'messageSender' must not be null");
		this.uri = uri;
		this.messageSender = messageSender;
	}

	@Override
	public void send(WebServiceMessage message) throws IOException {
		this.expectation = this.messageSender.matchRequest(this.uri, message);
		this.request = message;
	}

	@Override
	public @Nullable WebServiceMessage receive(WebServiceMessageFactory messageFactory) throws IOException {
		ResponseCreator responseCreator = getResponseCreator();
		if (responseCreator != null) {
			return responseCreator.createResponse(this.uri, Objects.requireNonNull(this.request), messageFactory);
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
		return getResponseCreator() instanceof ErrorResponseCreator;
	}

	@Override
	public @Nullable String getErrorMessage() throws IOException {
		if (getResponseCreator() instanceof ErrorResponseCreator errorResponseCreator) {
			return errorResponseCreator.getErrorMessage();
		}
		else {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		this.request = null;
		this.expectation = null;
	}

	private @Nullable ResponseCreator getResponseCreator() {
		return (this.expectation != null) ? this.expectation.getResponseCreator() : null;
	}

}
