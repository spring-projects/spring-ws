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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractHttpWebServiceMessageSender}.
 *
 * @author Stephane Nicoll
 */
class AbstractHttpWebServiceMessageSenderTests {

	private final AbstractHttpWebServiceMessageSender sender = new TestAbstractHttpWebServiceMessageSender();

	@Test
	void applicationAllowsLoopback() {
		assertThat(this.sender.supports(URI.create("http://127.0.0.1:8080/x"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void allowsPublicHttps() {
		assertThat(this.sender.supports(URI.create("https://example.com/path"), UriSource.REMOTE)).isTrue();
	}

	@Test
	void blocksLinkLocal() {
		assertThat(this.sender.supports(URI.create("http://169.254.1.2/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksLoopbackLiteral() {
		assertThat(this.sender.supports(URI.create("http://127.0.0.1:8080/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksLocalhost() {
		assertThat(this.sender.supports(URI.create("http://LocalHost/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksLocalhostSubdomain() {
		assertThat(this.sender.supports(URI.create("http://internal.localhost/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void rejectsNonHttpScheme() {
		assertThat(this.sender.supports(URI.create("file:///etc/passwd"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void rejectsUriWithoutHost() {
		assertThat(this.sender.supports(URI.create("http:opaque"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksIpv6Loopback() {
		assertThat(this.sender.supports(URI.create("http://[::1]/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksIpv4MappedLoopbackLiteral() {
		assertThat(this.sender.supports(URI.create("http://[0:0:0:0:0:ffff:127.0.0.1]/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksAnyLocalIpv4() {
		assertThat(this.sender.supports(URI.create("http://0.0.0.0/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void blocksIpv6LinkLocalLiteral() {
		assertThat(this.sender.supports(URI.create("http://[fe80::1]/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void rfc1918BlockedByDefault() {
		assertThat(this.sender.supports(URI.create("http://192.168.0.1/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void rfc1918AllowedWhenConfigured() {
		this.sender.setAllowSiteLocalIpv4(true);
		assertThat(this.sender.supports(URI.create("http://192.168.0.1/x"), UriSource.REMOTE)).isTrue();
	}

	@Test
	void supportedUriFilterCanBeCustomized() {
		TestAbstractHttpWebServiceMessageSender customSender = new TestAbstractHttpWebServiceMessageSender(
				(candidate) -> "https".equalsIgnoreCase(candidate.getScheme()));
		assertThat(customSender.supports(URI.create("http://example.org/x"), UriSource.APPLICATION)).isFalse();
		assertThat(customSender.supports(URI.create("https://example.org/x"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void remoteHttpsOnlyWhenPolicyRequiresSecureScheme() {
		this.sender.setDestinationPolicy((details,
				defaultChecks) -> defaultChecks.and(d -> "https".equalsIgnoreCase(d.uri().getScheme())).test(details));
		assertThat(this.sender.supports(URI.create("http://example.org/x"), UriSource.REMOTE)).isFalse();
		assertThat(this.sender.supports(URI.create("https://example.org/x"), UriSource.REMOTE)).isTrue();
	}

	@Test
	void destinationPolicyUsesDescriptorHostName() {
		this.sender.setDestinationPolicy((details, defaultChecks) -> defaultChecks
			.and((ignored) -> "example.org".equalsIgnoreCase(details.hostName()))
			.test(details));
		assertThat(this.sender.supports(URI.create("http://example.org/x"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("http://example.com/x"), UriSource.REMOTE)).isFalse();
	}

	private static final class TestAbstractHttpWebServiceMessageSender extends AbstractHttpWebServiceMessageSender {

		private TestAbstractHttpWebServiceMessageSender(Predicate<URI> supportedUriFilter) {
			super(supportedUriFilter);
		}

		public TestAbstractHttpWebServiceMessageSender() {
		}

		@Override
		public WebServiceConnection createConnection(URI uri) throws IOException {
			throw new UnsupportedEncodingException();
		}

	}

}
