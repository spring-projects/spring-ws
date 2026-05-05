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

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XmppMessageSender}.
 *
 * @author Stephane Nicoll
 */
class XmppMessageSenderSupportsTest {

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
	void remoteRejectsWhenNoConnectionConfigured() {
		assertThat(this.sender.supports(URI.create("xmpp:user@example.org"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void destinationPolicyUsesJabberIdDomain() {
		this.sender.setDestinationPolicy((descriptor, defaultChecks) -> descriptor.domain() != null
				&& descriptor.domain().equalsIgnoreCase("example.org"));
		assertThat(this.sender.supports(URI.create("xmpp:user@example.org"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("xmpp:user@other.org"), UriSource.REMOTE)).isFalse();
	}

}
