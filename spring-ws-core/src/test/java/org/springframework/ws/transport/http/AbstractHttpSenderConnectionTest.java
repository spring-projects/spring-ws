/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.http;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

/**
 * @author Andreas Veithen
 */
public class AbstractHttpSenderConnectionTest {

	/**
	 * Tests that {@link AbstractHttpSenderConnection} doesn't consume the response stream before passing it to the
	 * message factory. This is a regression test for SWS-707.
	 *
	 * @param chunking Specifies whether the test should simulate a response with chunking enabled.
	 * @throws Exception
	 */
	private void testSupportsStreaming(boolean chunking) throws Exception {

		byte[] content = new byte[16 * 1024];
		new Random().nextBytes(content);
		CountingInputStream rawInputStream = new CountingInputStream(new ByteArrayInputStream(content));

		AbstractHttpSenderConnection connection = spy(AbstractHttpSenderConnection.class);
		when(connection.getResponseCode()).thenReturn(200);
		// Simulate response with chunking enabled
		when(connection.getResponseContentLength()).thenReturn(chunking ? -1L : content.length);
		when(connection.getRawResponseInputStream()).thenReturn(rawInputStream);
		when(connection.getResponseHeaders(any())).thenReturn(Collections.emptyIterator());

		// Create a mock message factory to capture the InputStream passed to it
		WebServiceMessageFactory messageFactory = spy(WebServiceMessageFactory.class);
		WebServiceMessage message = spy(WebServiceMessage.class);

		ArgumentCaptor<InputStream> inputStreamCapture = ArgumentCaptor.forClass(InputStream.class);
		when(messageFactory.createWebServiceMessage(inputStreamCapture.capture())).thenReturn(message);

		connection.receive(messageFactory);

		assertThat(rawInputStream.getCount()).isLessThan(content.length);
		assertThat(IOUtils.toByteArray(inputStreamCapture.getValue())).isEqualTo(content);
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
