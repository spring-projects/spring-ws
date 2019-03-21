/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.ws.transport.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.easymock.Capture;
import org.junit.Test;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @author Andreas Veithen
 */
public class AbstractHttpSenderConnectionTest {
	
	/**
	 * Tests that {@link AbstractHttpSenderConnection} doesn't consume the response stream before
	 * passing it to the message factory. This is a regression test for SWS-707.
	 *
	 * @param chunking
	 *            Specifies whether the test should simulate a response with chunking enabled.
	 * @throws Exception
	 */
	private void testSupportsStreaming(boolean chunking) throws Exception {
		byte[] content = new byte[16*1024];
		new Random().nextBytes(content);
		CountingInputStream rawInputStream = new CountingInputStream(new ByteArrayInputStream(content));

		AbstractHttpSenderConnection connection = createNiceMock(AbstractHttpSenderConnection.class);
		expect(connection.getResponseCode()).andReturn(200);
		// Simulate response with chunking enabled
		expect(connection.getResponseContentLength()).andReturn(chunking ? -1L : content.length);
		expect(connection.getRawResponseInputStream()).andReturn(rawInputStream);
		expect(connection.getResponseHeaders(anyObject())).andReturn(Collections.emptyIterator());

		// Create a mock message factory to capture the InputStream passed to it
		WebServiceMessageFactory messageFactory = createNiceMock(WebServiceMessageFactory.class);
		WebServiceMessage message = createNiceMock(WebServiceMessage.class);
		Capture<InputStream> inputStreamCapture = new Capture<>();
		expect(messageFactory.createWebServiceMessage(capture(inputStreamCapture))).andReturn(message);

		replay(connection, messageFactory, message);

		connection.receive(messageFactory);

		assertTrue("The raw input stream has been completely consumed",
				rawInputStream.getCount() < content.length);
		assertArrayEquals("Unexpected content received by the message factory",
				content, IOUtils.toByteArray(inputStreamCapture.getValue()));
	}

	@Test
	public void testSupportsStreamingWithChunkingEnabled() throws Exception {
		testSupportsStreaming(true);
	}

	@Test
	public void testSupportsStreamingWithChunkingDisabled() throws Exception {
		testSupportsStreaming(false);
	}
}