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

package org.springframework.ws.transport.http;

import java.net.URI;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HttpDestinationDescriptor}.
 *
 * @author Stephane Nicoll
 */
class HttpDestinationDescriptorTests {

	@Test
	void siteLocalIpv4LiteralDetects24BitPrivate() {
		assertThat(createDescriptor("https://192.168.0.1/x").isSiteLocalIpv4Literal()).isTrue();
	}

	@Test
	void siteLocalIpv4LiteralDetectsClassA() {
		assertThat(createDescriptor("https://10.0.0.1/x").isSiteLocalIpv4Literal()).isTrue();
	}

	@Test
	void siteLocalIpv4LiteralDetectsClassBPrivate() {
		assertThat(createDescriptor("https://172.20.0.1/x").isSiteLocalIpv4Literal()).isTrue();
	}

	@Test
	void siteLocalIpv4LiteralIsFalseOutsideRfc1918Space() {
		assertThat(createDescriptor("https://172.32.0.1/x").isSiteLocalIpv4Literal()).isFalse();
	}

	@Test
	void siteLocalIpv4LiteralIsFalseForPublicLiteral() {
		assertThat(createDescriptor("https://198.51.100.42/x").isSiteLocalIpv4Literal()).isFalse();
	}

	@Test
	void siteLocalIpv4LiteralIsFalseForHostName() {
		assertThat(createDescriptor("https://example.org/x").isSiteLocalIpv4Literal()).isFalse();
	}

	@Test
	void siteLocalIpv4LiteralIsFalseForLoopbackLiteral() {
		assertThat(createDescriptor("https://127.0.0.1/x").isSiteLocalIpv4Literal()).isFalse();
	}

	private HttpDestinationDescriptor createDescriptor(String uri) {
		return HttpDestinationDescriptor.of(URI.create(uri), UriSource.APPLICATION);
	}

}
