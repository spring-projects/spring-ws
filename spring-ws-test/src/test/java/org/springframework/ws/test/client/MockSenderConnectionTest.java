/*
 * Copyright 2005-2010 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.ws.test.client.ResponseCreators.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.xml.transform.StringSource;

public class MockSenderConnectionTest {

	@Test
	public void error() throws IOException {

		String testErrorMessage = "Test Error Message";
		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withError(testErrorMessage));

		assertThat(connection.hasError()).isTrue();
		assertThat(connection.getErrorMessage()).isEqualTo(testErrorMessage);
	}

	@Test
	public void normal() throws IOException {

		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withPayload(new StringSource("<response/>")));

		assertThat(connection.hasError()).isFalse();
		assertThat(connection.getErrorMessage()).isNull();
	}

	@Test
	public void noRequestMatchers() {

		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {

			MockSenderConnection connection = new MockSenderConnection();
			connection.andRespond(withPayload(new StringSource("<response/>")));
			connection.send(null);
		});
	}
}
