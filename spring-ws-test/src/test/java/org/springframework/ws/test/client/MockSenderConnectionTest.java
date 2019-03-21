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

import java.io.IOException;

import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.springframework.ws.test.client.ResponseCreators.withError;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

public class MockSenderConnectionTest {

	@Test
	public void error() throws IOException {
		String testErrorMessage = "Test Error Message";
		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withError(testErrorMessage));
		assertTrue(connection.hasError());
		assertEquals(testErrorMessage, connection.getErrorMessage());
	}

	@Test
	public void normal() throws IOException {
		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withPayload(new StringSource("<response/>")));
		assertFalse(connection.hasError());
		assertNull(connection.getErrorMessage());
	}

	@Test(expected = AssertionError.class)
	public void noRequestMatchers() throws IOException {
		MockSenderConnection connection = new MockSenderConnection();
		connection.andRespond(withPayload(new StringSource("<response/>")));
		connection.send(null);
	}
}
