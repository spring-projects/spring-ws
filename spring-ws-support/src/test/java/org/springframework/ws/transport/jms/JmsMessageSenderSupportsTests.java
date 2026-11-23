/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JmsMessageSender}.
 *
 * @author Stephane Nicoll
 */
class JmsMessageSenderSupportsTest {

	private final JmsMessageSender sender = new JmsMessageSender();

	@Test
	void rejectsNonJmsScheme() {
		assertThat(this.sender.supports(URI.create("http://example.org/service"), UriSource.APPLICATION)).isFalse();
	}

	@Test
	void applicationAllowsAnyJmsUri() {
		assertThat(this.sender.supports(URI.create("jms:RequestQueue"), UriSource.APPLICATION)).isTrue();
		assertThat(this.sender.supports(URI.create("jms:ldap://evil/a"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void supportsRemoteDestinationsReflectsWhetherAPolicyIsConfigured() {
		assertThat(this.sender.supportsRemoteDestinations()).isFalse();
		this.sender.setDestinationPolicy((descriptor) -> true);
		assertThat(this.sender.supportsRemoteDestinations()).isTrue();
	}

	@Test
	void remoteRejectsPlainDestinationWithoutPolicy() {
		URI uri = URI.create("jms:RequestQueue?replyToName=ResponseQueueName");
		assertThat(this.sender.supports(uri, UriSource.REMOTE)).isFalse();
	}

	@Test
	void remoteUsesConfiguredPolicy() {
		this.sender.setDestinationPolicy((descriptor) -> "RequestQueue".equals(descriptor.destinationName()));
		assertThat(this.sender.supports(URI.create("jms:RequestQueue"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("jms:OtherQueue"), UriSource.REMOTE)).isFalse();
	}

}
