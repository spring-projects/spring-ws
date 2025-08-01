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
