/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
	public boolean supports(URI uri, UriSource uriSource) {
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
