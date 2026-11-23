/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.test.client;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.ws.test.client.ResponseCreators.withError;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

class MockSenderConnectionTests {

	@Test
	void error() throws IOException {

		String testErrorMessage = "Test Error Message";
		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withError(testErrorMessage));

		assertThat(connection.hasError()).isTrue();
		assertThat(connection.getErrorMessage()).isEqualTo(testErrorMessage);
	}

	@Test
	void normal() throws IOException {

		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withPayload(new StringSource("<response/>")));

		assertThat(connection.hasError()).isFalse();
		assertThat(connection.getErrorMessage()).isNull();
	}

	@Test
	void noRequestMatchers() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			MockSenderConnection connection = new MockSenderConnection();
			connection.andRespond(withPayload(new StringSource("<response/>")));
			connection.send(null);
		});
	}

}
