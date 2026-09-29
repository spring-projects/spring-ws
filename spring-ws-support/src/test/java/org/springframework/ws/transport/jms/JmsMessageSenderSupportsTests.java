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
