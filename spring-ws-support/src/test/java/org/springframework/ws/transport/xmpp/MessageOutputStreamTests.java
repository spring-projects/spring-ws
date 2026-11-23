/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jivesoftware.smack.packet.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for {@link MessageOutputStream}.
 *
 * @author Stephane Nicoll
 */
class MessageOutputStreamTests {

	@Test
	void flushSetsBodyOnMessage() throws IOException {
		Message message = new Message();
		MessageOutputStream outputStream = new MessageOutputStream(message, "UTF-8");
		outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		assertThat(message.getBody()).isEqualTo("hello");
	}

	@Test
	void flushCanBeCalledMultipleTimes() {
		Message message = new Message();
		MessageOutputStream outputStream = new MessageOutputStream(message, "UTF-8");
		assertThatCode(() -> {
			outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			outputStream.flush();
		}).doesNotThrowAnyException();
		assertThat(message.getBody()).isEqualTo("hello");
	}

	@Test
	void flushAfterFurtherWritesReplacesPreviousBody() throws IOException {
		Message message = new Message();
		MessageOutputStream outputStream = new MessageOutputStream(message, "UTF-8");
		outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		outputStream.write(" world".getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		assertThat(message.getBody()).isEqualTo("hello world");
		assertThat(message.getBodies()).hasSize(1);
	}

	@Test
	void closeFlushesPendingContent() throws IOException {
		Message message = new Message();
		MessageOutputStream outputStream = new MessageOutputStream(message, "UTF-8");
		outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
		outputStream.close();
		assertThat(message.getBody()).isEqualTo("hello");
	}

}
