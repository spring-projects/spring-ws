/*
 * Copyright 2005-2025 the original author or authors.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * Mock implementation of {@link WebServiceMessageSender}. Contains a list of expected
 * {@link MockSenderConnection}s, and iterates over those.
 *
 * @author Arjen Poutsma
 * @author Lukas Krecan
 * @author Greg Turnquist
 * @since 2.0
 */
public class MockWebServiceMessageSender implements WebServiceMessageSender {

	private final List<MockSenderConnection> expectedConnections = new LinkedList<>();

	private @Nullable Iterator<MockSenderConnection> connectionIterator;

	@Override
	public MockSenderConnection createConnection(URI uri) throws IOException {
		Assert.notNull(uri, "'uri' must not be null");
		if (this.connectionIterator == null) {
			this.connectionIterator = this.expectedConnections.iterator();
		}
		if (!this.connectionIterator.hasNext()) {
			throw new AssertionError("No further connections expected");
		}

		MockSenderConnection currentConnection = this.connectionIterator.next();
		currentConnection.setUri(uri);
		return currentConnection;
	}

	/**
	 * Always returns {@code true}.
	 */
	@Override
	public boolean supports(URI uri) {
		return true;
	}

	MockSenderConnection expectNewConnection() {
		Assert.state(this.connectionIterator == null,
				"Can not expect another connection, the test is already underway");
		MockSenderConnection connection = new MockSenderConnection();
		this.expectedConnections.add(connection);
		return connection;
	}

	void verifyConnections() {
		if (this.expectedConnections.isEmpty()) {
			return;
		}
		if (this.connectionIterator == null || this.connectionIterator.hasNext()) {
			throw new AssertionError("Further connection(s) expected");
		}
	}

	void reset() {
		this.expectedConnections.clear();
		this.connectionIterator = null;
	}

}
