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

import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link XmppSenderConnection}.
 *
 * @author Stephane Nicoll
 */
class XmppSenderConnectionTests {

	private static final StanzaFactory STANZA_FACTORY = new StanzaFactory(StandardStanzaIdSource.DEFAULT);

	private final XMPPConnection connection = mock(XMPPConnection.class);

	@BeforeEach
	void setUp() throws Exception {
		given(this.connection.getStanzaFactory()).willReturn(STANZA_FACTORY);
		given(this.connection.getUser()).willReturn(JidCreate.entityFullFrom("server@example.org/resource"));
	}

	@Test
	void constructorRejectsEmptyTo() {
		assertThatIllegalArgumentException().isThrownBy(() -> createConnection("", "thread-1"));
	}

	@Test
	void constructorRejectsEmptyThread() {
		assertThatIllegalArgumentException().isThrownBy(() -> createConnection("user@example.org", ""));
	}

	@Test
	void requestMessageHasDestinationTypeAndThread() {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		Message requestMessage = connection.getRequestMessage();
		assertThat(requestMessage.getTo().toString()).isEqualTo("user@example.org");
		assertThat(requestMessage.getType()).isEqualTo(Message.Type.chat);
		assertThat(requestMessage.getThread()).isEqualTo("thread-1");
	}

	@Test
	void getUriReturnsXmppUriForDestination() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		assertThat(connection.getUri()).isEqualTo(URI.create("xmpp:user@example.org"));
	}

	@Test
	void addRequestHeaderIsVisibleOnRequestMessage() {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		connection.addRequestHeader("name", "value");
		assertThat(connection.getRequestMessage().getBody()).isNull();
		assertThat(XmppTransportUtils.getHeaderNames(connection.getRequestMessage())).toIterable()
			.containsExactly("name");
	}

	@Test
	void hasErrorAndErrorMessageAreEmptyBeforeResponseIsReceived() {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		assertThat(connection.hasError()).isFalse();
		assertThat(connection.getErrorMessage()).isNull();
	}

	@Test
	void sendWritesBodySetsFromAndSendsStanza() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		connection.send(new StubMessage("hello"));
		Message sent = verifySentMessage();
		assertThat(sent.getBody()).isEqualTo("hello");
		assertThat(sent.getFrom().toString()).isEqualTo("server@example.org/resource");
		assertThat(sent.getTo().toString()).isEqualTo("user@example.org");
		assertThat(sent.getThread()).isEqualTo("thread-1");
	}

	@Test
	void sendSurvivesOutputStreamFlushedMultipleTimes() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		connection.send(new StubMessage("hello", 2));
		Message sent = verifySentMessage();
		assertThat(sent.getBody()).isEqualTo("hello");
		assertThat(sent.getBodies()).hasSize(1);
	}

	@Test
	void onReceiveBeforeReadUsesDefaultTimeoutByDefault() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		Message response = StanzaBuilder.buildMessage().ofType(Message.Type.chat).setBody("reply").build();
		StanzaCollector collector = mock(StanzaCollector.class);
		given(this.connection.createStanzaCollector(any(StanzaFilter.class))).willReturn(collector);
		given(collector.nextResult(0L)).willReturn(response);

		connection.onReceiveBeforeRead();

		assertThat(connection.hasResponse()).isTrue();
		assertThat(connection.getResponseMessage().getBody()).isEqualTo("reply");
	}

	@Test
	void onReceiveBeforeReadUsesConfiguredTimeout() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		connection.setReceiveTimeout(5000L);
		Message response = StanzaBuilder.buildMessage().ofType(Message.Type.chat).setBody("reply").build();
		StanzaCollector collector = mock(StanzaCollector.class);
		given(this.connection.createStanzaCollector(any(StanzaFilter.class))).willReturn(collector);
		given(collector.nextResult(5000L)).willReturn(response);

		connection.onReceiveBeforeRead();

		assertThat(connection.getResponseMessage().getBody()).isEqualTo("reply");
	}

	@Test
	void onReceiveBeforeReadUsesBlockingWaitForNegativeTimeout() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		connection.setReceiveTimeout(-1L);
		Message response = StanzaBuilder.buildMessage().ofType(Message.Type.chat).setBody("reply").build();
		StanzaCollector collector = mock(StanzaCollector.class);
		given(this.connection.createStanzaCollector(any(StanzaFilter.class))).willReturn(collector);
		given(collector.nextResult()).willReturn(response);

		connection.onReceiveBeforeRead();

		assertThat(connection.getResponseMessage().getBody()).isEqualTo("reply");
	}

	@Test
	void onReceiveBeforeReadWithoutResultLeavesResponseAbsent() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		StanzaCollector collector = mock(StanzaCollector.class);
		given(this.connection.createStanzaCollector(any(StanzaFilter.class))).willReturn(collector);
		given(collector.nextResult(0L)).willReturn(null);

		connection.onReceiveBeforeRead();

		assertThat(connection.hasResponse()).isFalse();
		assertThat(connection.getResponseMessage()).isNull();
	}

	@Test
	void onReceiveBeforeReadRejectsNonMessageStanza() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		StanzaCollector collector = mock(StanzaCollector.class);
		given(this.connection.createStanzaCollector(any(StanzaFilter.class))).willReturn(collector);
		Presence presence = StanzaBuilder.buildPresence().build();
		given(collector.nextResult(0L)).willReturn((Stanza) presence);

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(connection::onReceiveBeforeRead)
			.withMessageContaining("Wrong packet type");
	}

	@Test
	void responseErrorReflectsResponseMessageType() throws Exception {
		XmppSenderConnection connection = createConnection("user@example.org", "thread-1");
		Message response = StanzaBuilder.buildMessage().ofType(Message.Type.error).setBody("boom").build();
		StanzaCollector collector = mock(StanzaCollector.class);
		given(this.connection.createStanzaCollector(any(StanzaFilter.class))).willReturn(collector);
		given(collector.nextResult(0L)).willReturn(response);

		connection.onReceiveBeforeRead();

		assertThat(connection.hasError()).isTrue();
		assertThat(connection.getErrorMessage()).isEqualTo("boom");
	}

	private Message verifySentMessage() throws Exception {
		ArgumentCaptor<Stanza> captor = ArgumentCaptor.forClass(Stanza.class);
		verify(this.connection).sendStanza(captor.capture());
		return (Message) captor.getValue();
	}

	private XmppSenderConnection createConnection(String to, String thread) {
		return new XmppSenderConnection(this.connection, to, thread);
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
