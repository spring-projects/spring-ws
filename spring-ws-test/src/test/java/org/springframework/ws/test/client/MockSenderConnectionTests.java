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

import java.net.URI;

import jakarta.xml.soap.MessageFactory;
import org.junit.jupiter.api.Test;

import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.ws.test.client.RequestMatchers.anything;
import static org.springframework.ws.test.client.ResponseCreators.withError;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

class MockSenderConnectionTests {

	private final MockWebServiceMessageSender messageSender = new MockWebServiceMessageSender();

	@Test
	void error() throws Exception {

		String testErrorMessage = "Test Error Message";
		this.messageSender.expect(ExpectedCount.once()).andExpect(anything()).andRespond(withError(testErrorMessage));

		MockSenderConnection connection = sendRequest();

		assertThat(connection.hasError()).isTrue();
		assertThat(connection.getErrorMessage()).isEqualTo(testErrorMessage);
	}

	@Test
	void normal() throws Exception {

		this.messageSender.expect(ExpectedCount.once())
			.andExpect(anything())
			.andRespond(withPayload(new StringSource("<response/>")));

		MockSenderConnection connection = sendRequest();

		assertThat(connection.hasError()).isFalse();
		assertThat(connection.getErrorMessage()).isNull();
	}

	@Test
	void noRequestMatchers() {

		this.messageSender.expect(ExpectedCount.once()).andRespond(withPayload(new StringSource("<response/>")));

		assertThatExceptionOfType(AssertionError.class).isThrownBy(this::sendRequest);
	}

	private MockSenderConnection sendRequest() throws Exception {
		MockSenderConnection connection = this.messageSender.createConnection(URI.create("http://example.com"));
		connection.send(new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage());
		return connection;
	}

}
