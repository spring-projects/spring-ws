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

package org.springframework.ws.transport;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Tests for {@link AbstractWebServiceMessageSender}.
 *
 * @author Stephane Nicoll
 */
class AbstractWebServiceMessageSenderTests {

	private static final DestinationPolicy<TestDestinationDescriptor> REJECT_ALL_DESTINATION_POLICY = (
			descriptor) -> false;

	private static final URI VALID_URI_SCHEME = URI.create("https://example.com");

	@Test
	void supportsRemoteDestinationIsFalseByDefault() {
		assertThat(createTestSender().supportsRemoteDestinations()).isFalse();
	}

	@Test
	void supportsRemoteDestinationIsTrueWhenDestinationPolicyIsConfigured() {
		TestWebServiceMessageSender sender = createTestSender();
		sender.setDestinationPolicy(REJECT_ALL_DESTINATION_POLICY);
		assertThat(sender.supportsRemoteDestinations()).isTrue();
	}

	@Test
	void getDestinationPolicyReturnsNullByDefault() {
		assertThat(createTestSender().getDestinationPolicy()).isNull();
	}

	@Test
	void getDestinationPolicyReturnsDestinationPolicyWhenConfigured() {
		TestWebServiceMessageSender sender = createTestSender();
		sender.setDestinationPolicy(REJECT_ALL_DESTINATION_POLICY);
		assertThat(sender.getDestinationPolicy()).isSameAs(REJECT_ALL_DESTINATION_POLICY);
	}

	@ParameterizedTest
	@EnumSource(UriSource.class)
	void supportsWhenFilterIsFalseRejects(UriSource uriSource) {
		TestWebServiceMessageSender sender = new TestWebServiceMessageSender((uri) -> "https".equals(uri.getScheme()));
		assertThat(sender.supports(URI.create("http://example.com"), uriSource)).isFalse();
	}

	@Test
	void supportsWhenNoDestinationPolicyIsConfiguredAcceptsApplicationUriSource() {
		TestWebServiceMessageSender sender = createTestSender();
		assertThat(sender.supports(VALID_URI_SCHEME, UriSource.APPLICATION)).isTrue();
	}

	@Test
	void supportsWhenNoDestinationPolicyIsConfiguredRejectsRemoteUriSource() {
		TestWebServiceMessageSender sender = createTestSender();
		assertThat(sender.supports(VALID_URI_SCHEME, UriSource.REMOTE)).isFalse();
	}

	@ParameterizedTest
	@EnumSource(UriSource.class)
	void supportsWhenFilterIsTrueCallsDestinationPolicy(UriSource uriSource) {
		TestWebServiceMessageSender sender = createTestSender();
		DestinationPolicy<TestDestinationDescriptor> destinationPolicy = mockDestinationPolicy(VALID_URI_SCHEME,
				uriSource, true);
		sender.setDestinationPolicy(destinationPolicy);
		assertThat(sender.supports(VALID_URI_SCHEME, uriSource)).isTrue();
		verify(destinationPolicy);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	DestinationPolicy<TestDestinationDescriptor> mockDestinationPolicy(URI uri, UriSource uriSource, boolean outcome) {
		DestinationPolicy mock = createMock(DestinationPolicy.class);
		expect(mock.accept(eq(new TestDestinationDescriptor(uri, uriSource)))).andReturn(outcome);
		replay(mock);
		return mock;
	}

	TestWebServiceMessageSender createTestSender() {
		return new TestWebServiceMessageSender((uri) -> "https".equals(uri.getScheme()));
	}

	static class TestWebServiceMessageSender extends AbstractWebServiceMessageSender<TestDestinationDescriptor> {

		public TestWebServiceMessageSender(Predicate<URI> supportedUriFilter) {
			super(supportedUriFilter);
		}

		@Override
		protected TestDestinationDescriptor createDescriptor(URI uri, UriSource uriSource) {
			return new TestDestinationDescriptor(uri, uriSource);
		}

		@Override
		public WebServiceConnection createConnection(URI uri) throws IOException {
			return null;
		}

	}

	static class TestDestinationDescriptor implements WebServiceMessageSender.DestinationDescriptor {

		private final URI uri;

		private final UriSource uriSource;

		private TestDestinationDescriptor(URI uri, UriSource uriSource) {
			this.uri = uri;
			this.uriSource = uriSource;
		}

		@Override
		public URI uri() {
			return this.uri;
		}

		@Override
		public UriSource uriSource() {
			return this.uriSource;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TestDestinationDescriptor)) {
				return false;
			}
			TestDestinationDescriptor that = (TestDestinationDescriptor) other;
			return Objects.equals(this.uri, that.uri) && this.uriSource == that.uriSource;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.uri, this.uriSource);
		}

	}

}
