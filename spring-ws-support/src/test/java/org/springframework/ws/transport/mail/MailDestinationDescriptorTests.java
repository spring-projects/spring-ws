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
	void toAddressIsNullWhenMailtoCannotBeParsed() {
		assertThat(createDescriptor("mailto:@example.com").toAddress()).isNull();
	}

	private MailDestinationDescriptor createDescriptor(String uri) {
		return MailDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE);
	}

}
