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

package org.springframework.ws.transport.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AbstractHttpSenderConnection}.
 *
 * @author Andreas Veithen
 * @author Stephane Nicoll
 */
class AbstractHttpSenderConnectionTests {

	/**
	 * Tests that {@link AbstractHttpSenderConnection} doesn't consume the response stream
	 * before passing it to the message factory. This is a regression test for SWS-707.
	 * @param chunking Specifies whether the test should simulate a response with chunking
	 * enabled.
	 */
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void connectionSupportsStreaming(boolean chunking) throws Exception {
		byte[] content = new byte[16 * 1024];
		new Random().nextBytes(content);
		BoundedInputStream rawInputStream = BoundedInputStream.builder()
			.setInputStream(new ByteArrayInputStream(content))
			.get();
		AbstractHttpSenderConnection connection = spy(AbstractHttpSenderConnection.class);
		when(connection.getResponseCode()).thenReturn(200);
		// Simulate response with chunking if necessary
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
	void gzipResponseIsDecodedIfNecessary() throws IOException {
		ClassPathResource response = new ClassPathResource("soapRequest.xml", getClass());
		long responseSize = StreamUtils.copyToByteArray(response.getInputStream()).length;
		MultiValueMap<String, String> responseHeaders = MultiValueMap.fromSingleValue(
				Map.of(HttpTransportConstants.HEADER_CONTENT_ENCODING, HttpTransportConstants.CONTENT_ENCODING_GZIP,
						HttpTransportConstants.HEADER_CONTENT_LENGTH, String.valueOf(responseSize)));
		AbstractHttpSenderConnection connection = new TestHttpClientConnection(
				new ByteArrayInputStream(compress(response)), responseHeaders);
		assertThat(connection.hasResponse()).isTrue();
		assertThat(StreamUtils.copyToString(connection.getResponseInputStream(), StandardCharsets.UTF_8))
			.isEqualTo(StreamUtils.copyToString(response.getInputStream(), StandardCharsets.UTF_8));
	}

	private byte[] compress(Resource resource) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream in = resource.getInputStream(); GZIPOutputStream gzipOut = new GZIPOutputStream(out);) {
			StreamUtils.copy(in, gzipOut);
		}
		return out.toByteArray();
	}

	private static final class TestHttpClientConnection extends AbstractHttpSenderConnection {

		private final InputStream rawResponseInputStream;

		private final MultiValueMap<String, String> responseHeaders;

		public TestHttpClientConnection(InputStream rawResponseInputStream,
				MultiValueMap<String, String> responseHeaders) {
			this.rawResponseInputStream = rawResponseInputStream;
			this.responseHeaders = responseHeaders;
		}

		@Override
		protected int getResponseCode() {
			return HttpTransportConstants.STATUS_OK;
		}

		@Override
		protected String getResponseMessage() {
			return "";
		}

		@Override
		protected long getResponseContentLength() {
			return Long.parseLong(
					this.responseHeaders.getOrDefault(HttpTransportConstants.HEADER_CONTENT_LENGTH, List.of("-1"))
						.get(0));
		}

		@Override
		protected InputStream getRawResponseInputStream() {
			return this.rawResponseInputStream;
		}

		@Override
		protected OutputStream getRequestOutputStream() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<String> getResponseHeaderNames() {
			return this.responseHeaders.keySet().iterator();
		}

		@Override
		public Iterator<String> getResponseHeaders(String name) {
			return this.responseHeaders.getOrDefault(name, Collections.emptyList()).iterator();
		}

		@Override
		public void addRequestHeader(String name, String value) {

		}

		@Override
		public URI getUri() throws URISyntaxException {
			return null;
		}

	}

}
