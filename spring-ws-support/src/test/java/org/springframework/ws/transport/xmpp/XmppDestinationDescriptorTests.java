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
