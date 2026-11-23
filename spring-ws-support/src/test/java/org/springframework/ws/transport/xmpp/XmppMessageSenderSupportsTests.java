/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XmppMessageSender}.
 *
 * @author Stephane Nicoll
 */
class XmppMessageSenderSupportsTests {

	private final XmppMessageSender sender = new XmppMessageSender();

	@Test
	void rejectsNonXmppScheme() {
		assertThat(this.sender.supports(URI.create("http://example.org/service"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void applicationAllowsAnyXmppUri() {
		assertThat(this.sender.supports(URI.create("xmpp:user@example.org"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void supportsRemoteDestinationsReflectsWhetherAPolicyIsConfigured() {
		assertThat(this.sender.supportsRemoteDestinations()).isFalse();
		this.sender.setDestinationPolicy((descriptor) -> true);
		assertThat(this.sender.supportsRemoteDestinations()).isTrue();
	}

	@Test
	void remoteRejectsWithoutPolicy() {
		assertThat(this.sender.supports(URI.create("xmpp:user@example.org"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void remoteUsesConfiguredPolicy() {
		this.sender.setDestinationPolicy((descriptor) -> "user@example.org".equals(descriptor.jabberId()));
		assertThat(this.sender.supports(URI.create("xmpp:user@example.org"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("xmpp:user@other.org"), UriSource.REMOTE)).isFalse();
	}

}
