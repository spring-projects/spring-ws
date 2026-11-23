/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp.support;

import java.net.URI;
import java.net.URISyntaxException;

import org.jivesoftware.smack.packet.Message;
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
	void toUriFromMessage() throws URISyntaxException, XmppStringprepException {
		Message message = new Message(JidCreate.from("user@example.org"));
		assertThat(XmppTransportUtils.toUri(message)).isEqualTo(URI.create("xmpp:user@example.org"));
	}

	@Test
	void hasErrorIsFalseForNullMessage() {
		assertThat(XmppTransportUtils.hasError(null)).isFalse();
	}

	@Test
	void hasErrorIsFalseWhenTypeIsNotError() {
		Message message = new Message();
		message.setType(Message.Type.chat);
		assertThat(XmppTransportUtils.hasError(message)).isFalse();
	}

	@Test
	void hasErrorIsTrueWhenTypeIsError() {
		Message message = new Message();
		message.setType(Message.Type.error);
		assertThat(XmppTransportUtils.hasError(message)).isTrue();
	}

	@Test
	void getErrorMessageIsNullForNullMessage() {
		assertThat(XmppTransportUtils.getErrorMessage(null)).isNull();
	}

	@Test
	void getErrorMessageIsNullWhenTypeIsNotError() {
		Message message = new Message();
		message.setType(Message.Type.chat);
		message.setBody("failure");
		assertThat(XmppTransportUtils.getErrorMessage(message)).isNull();
	}

	@Test
	void getErrorMessageReturnsBodyWhenTypeIsError() {
		Message message = new Message();
		message.setType(Message.Type.error);
		message.setBody("failure");
		assertThat(XmppTransportUtils.getErrorMessage(message)).isEqualTo("failure");
	}

	@Test
	void addHeaderSetsPropertyOnMessage() {
		Message message = new Message();
		XmppTransportUtils.addHeader(message, "name", "value");
		assertThat(XmppTransportUtils.getHeaderNames(message)).toIterable().containsExactly("name");
		assertThat(XmppTransportUtils.getHeaders(message, "name")).toIterable().containsExactly("value");
	}

}
