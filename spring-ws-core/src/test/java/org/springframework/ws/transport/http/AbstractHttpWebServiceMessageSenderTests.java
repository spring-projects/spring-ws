/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	@ParameterizedTest
	@ValueSource(strings = { "https://example.com/path", "http://127.0.0.1:8080/x" })
	void applicationAllowsAnyHttpUri(String candidate) {
		assertThat(this.sender.supports(URI.create(candidate), UriSource.APPLICATION)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://example.com/path", "http://127.0.0.1:8080/x" })
	void remoteRejectsAnyHttpUriWithoutPolicy(String candidate) {
		assertThat(this.sender.supports(URI.create(candidate), UriSource.REMOTE)).isFalse();
	}

	@Test
	void rejectsNonHttpScheme() {
		assertThat(this.sender.supports(URI.create("file:///etc/passwd"), UriSource.REMOTE)).isFalse();
		assertThat(this.sender.supports(URI.create("file:///etc/passwd"), UriSource.APPLICATION)).isFalse();
	}

	@Test
	void supportedUriFilterIsAppliedBeforePolicy() {
		TestAbstractHttpWebServiceMessageSender customSender = new TestAbstractHttpWebServiceMessageSender(
				(candidate) -> "https".equalsIgnoreCase(candidate.getScheme()));
		customSender.setDestinationPolicy((descriptor) -> true);
		assertThat(customSender.supports(URI.create("http://example.org/x"), UriSource.APPLICATION)).isFalse();
		assertThat(customSender.supports(URI.create("https://example.org/x"), UriSource.APPLICATION)).isTrue();
	}

	@Test
	void remoteUsesConfiguredPolicy() {
		this.sender.setDestinationPolicy(
				(descriptor) -> "example.org".equals(descriptor.uri().getHost()) && descriptor.uri().getPort() == 8443);
		assertThat(this.sender.supports(URI.create("https://example.org:8443/x"), UriSource.REMOTE)).isTrue();
		assertThat(this.sender.supports(URI.create("https://example.org/x"), UriSource.REMOTE)).isFalse();
		assertThat(this.sender.supports(URI.create("https://example.com:8443/x"), UriSource.REMOTE)).isFalse();
	}

	@Test
	void policyIsConsultedForApplicationUriAsWell() {
		this.sender.setDestinationPolicy((descriptor) -> descriptor.uriSource() == UriSource.REMOTE);
		assertThat(this.sender.supports(URI.create("https://example.org/x"), UriSource.APPLICATION)).isFalse();
		assertThat(this.sender.supports(URI.create("https://example.org/x"), UriSource.REMOTE)).isTrue();
	}

	@Test
	void policySeesUriWithoutHost() {
		this.sender.setDestinationPolicy((descriptor) -> descriptor.uri().getHost() == null);
		assertThat(this.sender.supports(URI.create("http:opaque"), UriSource.REMOTE)).isTrue();
	}

	static class TestAbstractHttpWebServiceMessageSender extends AbstractHttpWebServiceMessageSender {

		TestAbstractHttpWebServiceMessageSender(Predicate<URI> supportedUriFilter) {
			super(supportedUriFilter);
		}

		TestAbstractHttpWebServiceMessageSender() {
		}

		@Override
		public WebServiceConnection createConnection(URI uri) throws IOException {
			throw new UnsupportedEncodingException();
		}

	}

}
