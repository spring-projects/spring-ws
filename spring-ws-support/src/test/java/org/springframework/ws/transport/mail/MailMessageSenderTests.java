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
	void remoteRejectsLocalhostMailboxHost() {
		assertThat(this.sender.supports(URI.create("mailto:user@localhost"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void remoteRejectsLocalhostSubdomainMailboxHost() {
		assertThat(this.sender.supports(URI.create("mailto:user@internal.localhost"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void remoteAcceptsPublicMailbox() {
		assertThat(this.sender.supports(URI.create("mailto:user@example.org"), UriSource.REMOTE)).isTrue();
	}

	@Test
	void applicationAllowsLocalhostMailbox() {
		assertThat(this.sender.supports(URI.create("mailto:user@localhost"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void destinationPolicyUsesMailboxHost() {
		this.sender.setDestinationPolicy(
				(descriptor, defaultChecks) -> defaultChecks.and(d -> "corp.example".equalsIgnoreCase(d.mailboxHost()))
					.test(descriptor));
		assertThat(this.sender.supports(URI.create("mailto:a@corp.example"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("mailto:a@other.example"), UriSource.REMOTE)).isFalse();
	}

}
