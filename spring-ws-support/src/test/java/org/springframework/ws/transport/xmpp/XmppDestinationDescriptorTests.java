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
class XmppDestinationDescriptorTest {

	@Test
	void ofExposesUriAndSource() {
		URI uri = URI.create("xmpp:user@example.org");
		XmppDestinationDescriptor descriptor = XmppDestinationDescriptor.of(uri, UriSource.REMOTE);
		assertThat(descriptor.uri()).isEqualTo(uri);
		assertThat(descriptor.uriSource()).isEqualTo(UriSource.REMOTE);
	}

	@Test
	void jabberIdIsSchemeSpecificPart() {
		XmppDestinationDescriptor descriptor = createDescriptor("xmpp:user@example.org");
		assertThat(descriptor.jabberId()).isEqualTo("user@example.org");
	}

	@Test
	void domainExtractedFromJid() {
		XmppDestinationDescriptor descriptor = createDescriptor("xmpp:user@example.org");
		assertThat(descriptor.domain()).isEqualTo("example.org");
	}

	@Test
	void domainIsLowercased() {
		XmppDestinationDescriptor descriptor = createDescriptor("xmpp:user@Example.ORG");
		assertThat(descriptor.domain()).isEqualTo("example.org");
	}

	@Test
	void domainStripsResourcePart() {
		XmppDestinationDescriptor descriptor = createDescriptor("xmpp:user@example.org/resource");
		assertThat(descriptor.domain()).isEqualTo("example.org");
	}

	@Test
	void domainIsNullWhenJidHasNoAtSign() {
		XmppDestinationDescriptor descriptor = createDescriptor("xmpp:noDomain");
		assertThat(descriptor.domain()).isNull();
	}

	@Test
	void domainIsNullWhenAtSignIsAtEnd() {
		XmppDestinationDescriptor descriptor = createDescriptor("xmpp:user@");
		assertThat(descriptor.domain()).isNull();
	}

	private XmppDestinationDescriptor createDescriptor(String uri) {
		return XmppDestinationDescriptor.of(URI.create(uri), UriSource.APPLICATION);
	}

}
