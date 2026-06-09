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
 * Tests for {@link MailDestinationDescriptor}.
 *
 * @author Stephane Nicoll
 */
class MailDestinationDescriptorTest {

	@Test
	void ofExposesUriAndSource() {
		URI uri = URI.create("mailto:user@example.com");
		MailDestinationDescriptor descriptor = MailDestinationDescriptor.of(uri, UriSource.REMOTE);
		assertThat(descriptor.uri()).isEqualTo(uri);
		assertThat(descriptor.uriSource()).isEqualTo(UriSource.REMOTE);
	}

	@Test
	void toAddressParsedFromValidMailto() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:user@example.com");
		assertThat(descriptor.toAddress()).isNotNull();
		assertThat(descriptor.toAddress().getAddress()).isEqualTo("user@example.com");
	}

	@Test
	void mailboxHostExtractedFromAddress() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:user@example.com");
		assertThat(descriptor.mailboxHost()).isEqualTo("example.com");
	}

	@Test
	void mailboxHostIsLowercased() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:User@Example.COM");
		assertThat(descriptor.mailboxHost()).isEqualTo("example.com");
	}

	@Test
	void mailboxHostIsNullWhenAddressHasNoAtSign() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:invalid");
		assertThat(descriptor.mailboxHost()).isNull();
	}

	@Test
	void mailboxHostIsNullWhenAtSignIsAtEnd() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:user@");
		assertThat(descriptor.mailboxHost()).isNull();
	}

	@Test
	void mailboxHostIsNullWhenAddressContainsNewline() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:user@example.com%0Abcc@evil.com");
		assertThat(descriptor.mailboxHost()).isNull();
	}

	@Test
	void mailboxHostIsNullWhenAddressContainsCarriageReturn() {
		MailDestinationDescriptor descriptor = createDescriptor("mailto:user@example.com%0Dbcc@evil.com");
		assertThat(descriptor.mailboxHost()).isNull();
	}

	private MailDestinationDescriptor createDescriptor(String uri) {
		return MailDestinationDescriptor.of(URI.create(uri), UriSource.APPLICATION);
	}

}
