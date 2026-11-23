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
 * Tests for {@link JmsDestinationDescriptor}.
 *
 * @author Stephane Nicoll
 */
class JmsDestinationDescriptorTest {

	@Test
	void ofExposesUriAndSource() {
		URI uri = URI.create("jms:RequestQueue");
		JmsDestinationDescriptor descriptor = JmsDestinationDescriptor.of(uri, UriSource.REMOTE);
		assertThat(descriptor.uri()).isEqualTo(uri);
		assertThat(descriptor.uriSource()).isEqualTo(UriSource.REMOTE);
	}

	@Test
	void destinationNameParsedFromSimpleUri() {
		assertThat(createDescriptor("jms:RequestQueue").destinationName()).isEqualTo("RequestQueue");
	}

	@Test
	void destinationNameParsedFromUriWithReplyToName() {
		assertThat(createDescriptor("jms:RequestQueue?replyToName=ResponseQueue").destinationName())
			.isEqualTo("RequestQueue");
	}

	@Test
	void replyToNameParsedFromUri() {
		assertThat(createDescriptor("jms:RequestQueue?replyToName=ResponseQueue").replyToName())
			.isEqualTo("ResponseQueue");
	}

	@Test
	void replyToNameIsNullWhenAbsent() {
		assertThat(createDescriptor("jms:RequestQueue").replyToName()).isNull();
	}

	private JmsDestinationDescriptor createDescriptor(String uri) {
		return JmsDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE);
	}

}
