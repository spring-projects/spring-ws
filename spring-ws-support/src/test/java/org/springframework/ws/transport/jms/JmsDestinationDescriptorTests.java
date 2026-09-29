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
