/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.pox.dom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import org.springframework.util.unit.DataSize;
import org.springframework.ws.AbstractWebServiceMessageFactoryTests;
import org.springframework.ws.WebServiceMessageFactory;

import static org.assertj.core.api.Assertions.assertThat;
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

	@Test
	void maxMessageSizeCanBeSetUsingStringRepresentation() {
		DomPoxMessageFactory factory = new DomPoxMessageFactory();
		factory.setMaxMessageSize("2MB");
		assertThat(factory).hasFieldOrPropertyWithValue("maxMessageSize", DataSize.ofMegabytes(2));
	}

	@Test
	void maxMessageSizeDefaultToBytes() {
		DomPoxMessageFactory factory = new DomPoxMessageFactory();
		factory.setMaxMessageSize("1024");
		assertThat(factory).hasFieldOrPropertyWithValue("maxMessageSize", DataSize.ofKilobytes(1));
	}

}
