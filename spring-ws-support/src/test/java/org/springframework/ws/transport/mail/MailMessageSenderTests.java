/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.mail;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MailMessageSender}.
 *
 * @author Stephane Nicoll
 */
class MailMessageSenderTests {

	private final MailMessageSender sender = new MailMessageSender();

	@Test
	void rejectsNonMailScheme() {
		assertThat(this.sender.supports(URI.create("http://example.org/service"), UriSource.APPLICATION)).isFalse();
	}

	@Test
	void applicationAllowsAnyMailbox() {
		assertThat(this.sender.supports(URI.create("mailto:user@localhost"), UriSource.APPLICATION)).isTrue();
		assertThat(this.sender.supports(URI.create("mailto:user@example.org"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void supportsRemoteDestinationsReflectsWhetherAPolicyIsConfigured() {
		assertThat(this.sender.supportsRemoteDestinations()).isFalse();
		this.sender.setDestinationPolicy((descriptor) -> true);
		assertThat(this.sender.supportsRemoteDestinations()).isTrue();
	}

	@Test
	void remoteRejectsPublicMailboxWithoutPolicy() {
		assertThat(this.sender.supports(URI.create("mailto:user@example.org"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void remoteUsesConfiguredPolicy() {
		this.sender.setDestinationPolicy((descriptor) -> descriptor.toAddress() != null
				&& "a@corp.example".equals(descriptor.toAddress().getAddress()));
		assertThat(this.sender.supports(URI.create("mailto:a@corp.example"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("mailto:a@other.example"), UriSource.REMOTE)).isFalse();
	}

}
