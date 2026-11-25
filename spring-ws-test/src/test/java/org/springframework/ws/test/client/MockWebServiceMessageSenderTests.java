/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.net.URI;

import jakarta.xml.soap.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.ws.test.client.RequestMatchers.anything;

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

			this.sender.expect(ExpectedCount.once());
			this.sender.verifyConnections();
		});
	}

	@Test
	void verifyMoteThanOne() throws Exception {
		this.sender.expect(ExpectedCount.once()).andExpect(anything());
		this.sender.expect(ExpectedCount.once()).andExpect(anything());
		this.sender.createConnection(URI.create("http://localhost"))
			.send(new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage());

		assertThatExceptionOfType(AssertionError.class).isThrownBy(this.sender::verifyConnections);
	}

}
