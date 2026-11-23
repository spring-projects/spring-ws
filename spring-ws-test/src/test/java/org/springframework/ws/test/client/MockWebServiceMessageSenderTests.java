/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MockWebServiceMessageSenderTests {

	private MockWebServiceMessageSender sender;

	@BeforeEach
	void setUp() {
		this.sender = new MockWebServiceMessageSender();
	}

	@Test
	void noMoreExpectedConnections() {

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> this.sender.createConnection(URI.create("http://localhost")));
	}

	@Test
	void verify() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			this.sender.expectNewConnection();
			this.sender.verifyConnections();
		});
	}

	@Test
	void verifyMoteThanOne() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			this.sender.expectNewConnection();
			this.sender.expectNewConnection();
			this.sender.createConnection(URI.create("http://localhost"));
			this.sender.verifyConnections();
		});
	}

}
