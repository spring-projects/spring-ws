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
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.StanzaBuilder;
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
	void flushSetsBodyOnMessageBuilder() throws IOException {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage();
		MessageOutputStream outputStream = new MessageOutputStream(messageBuilder, "UTF-8");
		outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		assertThat(messageBuilder.build().getBody()).isEqualTo("hello");
	}

	@Test
	void flushCanBeCalledMultipleTimes() {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage();
		MessageOutputStream outputStream = new MessageOutputStream(messageBuilder, "UTF-8");
		assertThatCode(() -> {
			outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
			outputStream.flush();
		}).doesNotThrowAnyException();
		assertThat(messageBuilder.build().getBody()).isEqualTo("hello");
	}

	@Test
	void flushAfterFurtherWritesReplacesPreviousBody() throws IOException {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage();
		MessageOutputStream outputStream = new MessageOutputStream(messageBuilder, "UTF-8");
		outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		outputStream.write(" world".getBytes(StandardCharsets.UTF_8));
		outputStream.flush();
		Message message = messageBuilder.build();
		assertThat(message.getBody()).isEqualTo("hello world");
		assertThat(message.getBodies()).hasSize(1);
	}

	@Test
	void closeFlushesPendingContent() throws IOException {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage();
		MessageOutputStream outputStream = new MessageOutputStream(messageBuilder, "UTF-8");
		outputStream.write("hello".getBytes(StandardCharsets.UTF_8));
		outputStream.close();
		assertThat(messageBuilder.build().getBody()).isEqualTo("hello");
	}

}
