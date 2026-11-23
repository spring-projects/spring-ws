/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.http;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link HttpDestinationPolicies}.
 *
 * @author Stephane Nicoll
 */
class HttpDestinationPoliciesTests {

	@Test
	void allowedHostsAcceptsListedHost() {
		assertThat(accept("replies.example.org", "https://replies.example.org/collect")).isTrue();
	}

	@Test
	void allowedHostsAcceptsListedHostOnAnyPortOrPath() {
		assertThat(accept("replies.example.org", "http://replies.example.org:8080/other")).isTrue();
	}

	@Test
	void allowedHostsAcceptsIpLiteral() {
		assertThat(accept("198.51.100.42", "http://198.51.100.42:8080/x")).isTrue();
	}

	@Test
	void allowedHostsRejectsOtherHost() {
		assertThat(accept("replies.example.org", "https://evil.example.com/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsSubDomain() {
		assertThat(accept("example.org", "https://evil.example.org/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsSuffixMatch() {
		assertThat(accept("example.org", "https://notexample.org/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsTrailingDot() {
		assertThat(accept("replies.example.org", "https://replies.example.org./x")).isFalse();
	}

	@Test
	void allowedHostsRejectsAsciiCaseDifference() {
		assertThat(accept("Replies.Example.ORG", "https://REPLIES.example.org/collect")).isFalse();
	}

	@Test
	void allowedHostsRejectsAnotherNotationForAnAllowedAddress() {
		assertThat(accept("127.0.0.1", "http://2130706433/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsInternationalizedFormOfAnAllowedPunycodeHost() {
		assertThat(accept("xn--mnchen-3ya.example", "https://münchen.example/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsNonAsciiHostThatCouldFoldOntoAnAllowedHost() {
		assertThat(accept("k.example.org", "https://K.example.org/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsUserInfoPointingAtAnAllowedHost() {
		assertThat(accept("replies.example.org", "https://replies.example.org@evil.example.com/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsUserInfoOnAnAllowedHost() {
		assertThat(accept("replies.example.org", "https://user:pw@replies.example.org/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsPortWithLeadingZero() {
		assertThat(accept("replies.example.org", "https://replies.example.org:0443/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsEmptyPortSeparator() {
		assertThat(accept("replies.example.org", "https://replies.example.org:/x")).isFalse();
	}

	@Test
	void allowedHostsRejectsUriWithoutHost() {
		assertThat(accept("example.org", "http:opaque")).isFalse();
	}

	@Test
	void allowedHostsRejectsRegistryBasedAuthority() {
		assertThat(accept("example.org", "http://my_service.example.org/x")).isFalse();
	}

	@Test
	void allowedHostsDoesNotConstrainApplicationDestinations() {
		DestinationPolicy<HttpDestinationDescriptor> policy = HttpDestinationPolicies.allowedHosts("example.org");
		assertThat(policy
			.accept(HttpDestinationDescriptor.of(URI.create("https://other.example.com/x"), UriSource.APPLICATION)))
			.isTrue();
	}

	@Test
	void allowedHostsRejectsEmptyHosts() {
		assertThatIllegalArgumentException().isThrownBy(HttpDestinationPolicies::allowedHosts)
			.withMessageContaining("'hosts' must not be empty");
	}

	@Test
	void allowedHostsRejectsEmptyHost() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> HttpDestinationPolicies.allowedHosts(Collections.singletonList("  ")))
			.withMessageContaining("'hosts' must not contain an empty host");
	}

	@ParameterizedTest
	@ValueSource(strings = { "https://replies.example.org", "replies.example.org:8443", "replies.example.org/collect",
			"*.example.org", "replies.example.org.", "user@replies.example.org", "münchen.example",
			"my_service.example.org", "[2001:db8::1]" })
	void allowedHostsRejectsHostThatCannotBeCompared(String host) {
		assertThatIllegalArgumentException().isThrownBy(() -> HttpDestinationPolicies.allowedHosts(host))
			.withMessageContaining("cannot be compared against the host of a destination");
	}

	private boolean accept(String allowedHost, String uri) {
		return HttpDestinationPolicies.allowedHosts(allowedHost)
			.accept(HttpDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE));
	}

}
