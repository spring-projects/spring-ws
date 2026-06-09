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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
		JmsDestinationDescriptor descriptor = createDescriptor("jms:RequestQueue");
		assertThat(descriptor.destinationName()).isEqualTo("RequestQueue");
	}

	@Test
	void destinationNameParsedFromUriWithReplyToName() {
		JmsDestinationDescriptor descriptor = createDescriptor("jms:RequestQueue?replyToName=ResponseQueue");
		assertThat(descriptor.destinationName()).isEqualTo("RequestQueue");
	}

	@Test
	void replyToNameParsedFromUri() {
		JmsDestinationDescriptor descriptor = createDescriptor("jms:RequestQueue?replyToName=ResponseQueue");
		assertThat(descriptor.replyToName()).isEqualTo("ResponseQueue");
	}

	@Test
	void replyToNameIsNullWhenAbsent() {
		JmsDestinationDescriptor descriptor = createDescriptor("jms:RequestQueue");
		assertThat(descriptor.replyToName()).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ldap:", "ldaps:", "rmi:", "dns:", "iiop:", "iiopname:", "corbaname:", "java:" })
	void containsSuspiciousJmsNameIndirectionDetectsInDestinationName(String prefix) {
		String uri = "jms:" + prefix + "evil/a";
		assertThat(createDescriptor(uri).containsSuspiciousJmsNameIndirection()).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "ldap:", "ldaps:", "rmi:", "dns:", "iiop:", "iiopname:", "corbaname:", "java:" })
	void containsSuspiciousJmsNameIndirectionDetectsInReplyToName(String prefix) {
		String uri = "jms:RequestQueue?replyToName=" + prefix + "evil/a";
		assertThat(createDescriptor(uri).containsSuspiciousJmsNameIndirection()).isTrue();
	}

	@Test
	void containsSuspiciousJmsNameIndirectionIsCaseInsensitive() {
		assertThat(createDescriptor("jms:LDAP://evil/a").containsSuspiciousJmsNameIndirection()).isTrue();
	}

	@Test
	void containsSuspiciousJmsNameIndirectionReturnsFalseForPlainNames() {
		assertThat(
				createDescriptor("jms:RequestQueue?replyToName=ResponseQueue").containsSuspiciousJmsNameIndirection())
			.isFalse();
	}

	private JmsDestinationDescriptor createDescriptor(String uri) {
		return JmsDestinationDescriptor.of(URI.create(uri), UriSource.APPLICATION);
	}

}
