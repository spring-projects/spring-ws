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
 * Tests for {@link XmppDestinationDescriptor}.
 *
 * @author Stephane Nicoll
 */
class XmppDestinationDescriptorTests {

	@Test
	void ofExposesUriAndSource() {
		URI uri = URI.create("xmpp:user@example.org");
		XmppDestinationDescriptor descriptor = XmppDestinationDescriptor.of(uri, UriSource.REMOTE);
		assertThat(descriptor.uri()).isEqualTo(uri);
		assertThat(descriptor.uriSource()).isEqualTo(UriSource.REMOTE);
	}

	@Test
	void jabberIdIsSchemeSpecificPart() {
		assertThat(createDescriptor("xmpp:user@example.org").jabberId()).isEqualTo("user@example.org");
	}

	@Test
	void jabberIdKeepsResource() {
		assertThat(createDescriptor("xmpp:user@example.org/mobile").jabberId()).isEqualTo("user@example.org/mobile");
	}

	private XmppDestinationDescriptor createDescriptor(String uri) {
		return XmppDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE);
	}

}
