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

package org.springframework.ws.transport.xmpp.support;

import java.net.URI;
import java.net.URISyntaxException;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.junit.jupiter.api.Test;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XmppTransportUtils}.
 *
 * @author Stephane Nicoll
 */
class XmppTransportUtilsTests {

	@Test
	void toUriFromMessageBuilder() throws URISyntaxException, XmppStringprepException {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage().to(JidCreate.from("user@example.org"));
		assertThat(XmppTransportUtils.toUri(messageBuilder)).isEqualTo(URI.create("xmpp:user@example.org"));
	}

	@Test
	void hasErrorIsFalseForNullMessageBuilder() {
		assertThat(XmppTransportUtils.hasError(null)).isFalse();
	}

	@Test
	void hasErrorIsFalseWhenTypeIsNotError() {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage().ofType(Message.Type.chat);
		assertThat(XmppTransportUtils.hasError(messageBuilder)).isFalse();
	}

	@Test
	void hasErrorIsTrueWhenTypeIsError() {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage().ofType(Message.Type.error);
		assertThat(XmppTransportUtils.hasError(messageBuilder)).isTrue();
	}

	@Test
	void getErrorMessageIsNullForNullMessageBuilder() {
		assertThat(XmppTransportUtils.getErrorMessage(null)).isNull();
	}

	@Test
	void getErrorMessageIsNullWhenTypeIsNotError() {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage().ofType(Message.Type.chat).setBody("failure");
		assertThat(XmppTransportUtils.getErrorMessage(messageBuilder)).isNull();
	}

	@Test
	void getErrorMessageReturnsBodyWhenTypeIsError() {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage().ofType(Message.Type.error).setBody("failure");
		assertThat(XmppTransportUtils.getErrorMessage(messageBuilder)).isEqualTo("failure");
	}

	@Test
	void addHeaderSetsPropertyOnMessageBuilder() {
		MessageBuilder messageBuilder = StanzaBuilder.buildMessage();
		XmppTransportUtils.addHeader(messageBuilder, "name", "value");
		Message message = messageBuilder.build();
		assertThat(XmppTransportUtils.getHeaderNames(message)).toIterable().containsExactly("name");
		assertThat(XmppTransportUtils.getHeaders(message, "name")).toIterable().containsExactly("value");
	}

}
