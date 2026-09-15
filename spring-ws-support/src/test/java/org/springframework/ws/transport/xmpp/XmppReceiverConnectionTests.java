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

package org.springframework.ws.transport.xmpp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smack.packet.StanzaFactory;
import org.jivesoftware.smack.packet.id.StandardStanzaIdSource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmpp.jid.impl.JidCreate;
import org.mockito.ArgumentCaptor;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link XmppReceiverConnection}.
 *
 * @author Stephane Nicoll
 */
class XmppReceiverConnectionTests {

	private static final StanzaFactory STANZA_FACTORY = new StanzaFactory(StandardStanzaIdSource.DEFAULT);

	private final XMPPConnection connection = mock(XMPPConnection.class);

	@BeforeEach
	void setUp() throws Exception {
		given(this.connection.getStanzaFactory()).willReturn(STANZA_FACTORY);
		given(this.connection.getUser()).willReturn(JidCreate.entityFullFrom("server@example.org/resource"));
	}

	@Test
	void constructorRejectsNullConnection() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new XmppReceiverConnection(null, createRequestMessage("client@example.org")));
	}

	@Test
	void constructorRejectsNullRequestMessage() {
		assertThatIllegalArgumentException().isThrownBy(() -> new XmppReceiverConnection(this.connection, null));
	}

	@Test
	void getRequestMessageReturnsGivenMessage() {
		Message requestMessage = createRequestMessage("client@example.org");
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection, requestMessage);
		assertThat(connection.getRequestMessage()).isSameAs(requestMessage);
	}

	@Test
	void getUriDelegatesToRequestMessage() throws Exception {
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection,
				createRequestMessage("client@example.org"));
		assertThat(connection.getUri()).isEqualTo(URI.create("xmpp:server@example.org"));
	}

	@Test
	void getRequestHeadersReflectRequestMessageProperties() {
		Message requestMessage = StanzaBuilder.buildMessage()
			.to(JidCreate.fromOrThrowUnchecked("server@example.org"))
			.ofType(Message.Type.chat)
			.setThread("thread-1")
			.build();
		requestMessage = addHeader(requestMessage, "name", "value");
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection, requestMessage);
		assertThat(XmppTransportUtils.getHeaderNames(connection.getRequestMessage())).toIterable()
			.containsExactly("name");
	}

	@Test
	void hasErrorAndErrorMessageAreEmptyBeforeResponseIsPrepared() {
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection,
				createRequestMessage("client@example.org"));
		assertThat(connection.hasError()).isFalse();
		assertThat(connection.getErrorMessage()).isNull();
	}

	@Test
	void sendCopiesDestinationFromAndThreadAndWritesBody() throws Exception {
		Message requestMessage = createRequestMessageWithThread("client@example.org/resource", "thread-1");
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection, requestMessage);

		connection.send(new StubMessage("hello"));

		Message sent = verifySentMessage();
		assertThat(sent.getBody()).isEqualTo("hello");
		assertThat(sent.getTo().toString()).isEqualTo("client@example.org/resource");
		assertThat(sent.getFrom().toString()).isEqualTo("server@example.org/resource");
		assertThat(sent.getThread()).isEqualTo("thread-1");
		assertThat(sent.getType()).isEqualTo(Message.Type.chat);
	}

	@Test
	void sendSurvivesOutputStreamFlushedMultipleTimes() throws Exception {
		Message requestMessage = createRequestMessage("client@example.org");
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection, requestMessage);

		connection.send(new StubMessage("hello", 2));

		Message sent = verifySentMessage();
		assertThat(sent.getBody()).isEqualTo("hello");
		assertThat(sent.getBodies()).hasSize(1);
	}

	@Test
	void addResponseHeaderIsVisibleOnResponseMessage() throws Exception {
		Message requestMessage = createRequestMessage("client@example.org");
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection, requestMessage);
		connection.onSendBeforeWrite(new StubMessage("hello"));

		connection.addResponseHeader("name", "value");

		assertThat(XmppTransportUtils.getHeaderNames(connection.getResponseMessage())).toIterable()
			.containsExactly("name");
	}

	@Test
	void hasErrorIsFalseAfterSendingAChatResponse() throws Exception {
		Message requestMessage = createRequestMessage("client@example.org");
		XmppReceiverConnection connection = new XmppReceiverConnection(this.connection, requestMessage);

		connection.send(new StubMessage("hello"));

		assertThat(connection.hasError()).isFalse();
		assertThat(connection.getErrorMessage()).isNull();
	}

	private Message verifySentMessage() throws Exception {
		ArgumentCaptor<Stanza> captor = ArgumentCaptor.forClass(Stanza.class);
		verify(this.connection).sendStanza(captor.capture());
		return (Message) captor.getValue();
	}

	private Message createRequestMessage(String from) {
		return createRequestMessageWithThread(from, "thread-1");
	}

	private Message createRequestMessageWithThread(String from, String thread) {
		return StanzaBuilder.buildMessage()
			.to(JidCreate.fromOrThrowUnchecked("server@example.org"))
			.from(JidCreate.fromOrThrowUnchecked(from))
			.ofType(Message.Type.chat)
			.setThread(thread)
			.build();
	}

	private Message addHeader(Message message, String name, String value) {
		MessageBuilder builder = STANZA_FACTORY.buildMessageStanzaFrom(message);
		XmppTransportUtils.addHeader(builder, name, value);
		return builder.build();
	}

	private static final class StubMessage implements WebServiceMessage {

		private final String body;

		private final int flushCount;

		StubMessage(String body) {
			this(body, 1);
		}

		StubMessage(String body, int flushCount) {
			this.body = body;
			this.flushCount = flushCount;
		}

		@Override
		public @Nullable Source getPayloadSource() {
			return null;
		}

		@Override
		public Result getPayloadResult() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void writeTo(OutputStream outputStream) throws IOException {
			outputStream.write(this.body.getBytes(StandardCharsets.UTF_8));
			for (int i = 0; i < this.flushCount; i++) {
				outputStream.flush();
			}
		}

	}

}
