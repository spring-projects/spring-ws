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

package org.springframework.ws.pox.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import org.springframework.util.unit.DataSize;
import org.springframework.ws.AbstractWebServiceMessageFactoryTests;
import org.springframework.ws.WebServiceMessageFactory;

import static org.assertj.core.api.Assertions.assertThatIOException;

class DomPoxMessageFactoryTests extends AbstractWebServiceMessageFactoryTests {

	@Override
	protected WebServiceMessageFactory createMessageFactory() {
		return new DomPoxMessageFactory();
	}

	@Test
	void testMaxMessageSizeExceeded() {
		DomPoxMessageFactory factory = new DomPoxMessageFactory();
		factory.setMaxMessageSize(DataSize.ofBytes(100));
		StringBuilder sb = new StringBuilder();
		sb.append("<test>");
		for (int i = 0; i < 100; i++) {
			sb.append("<item>AAAAAAAAAAAAAAAAAAAA</item>");
		}
		sb.append("</test>");
		InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
		assertThatIOException().isThrownBy(() -> factory.createWebServiceMessage(is))
			.withMessageContaining("Maximum message size exceeded");
	}

	@Test
	void testMaxGzipDecompressedSizeExceeded() throws Exception {
		DomPoxMessageFactory factory = new DomPoxMessageFactory();
		factory.setMaxMessageSize(DataSize.ofBytes(100));
		StringBuilder sb = new StringBuilder();
		sb.append("<test>");
		for (int i = 0; i < 100; i++) {
			sb.append("<item>AAAAAAAAAAAAAAAAAAAA</item>");
		}
		sb.append("</test>");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
			gzipOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
		}
		InputStream is = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
		assertThatIOException().isThrownBy(() -> factory.createWebServiceMessage(is))
			.withMessageContaining("Maximum message size exceeded");
	}

}
