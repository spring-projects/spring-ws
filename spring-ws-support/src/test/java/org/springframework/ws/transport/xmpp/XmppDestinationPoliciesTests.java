/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link XmppDestinationPolicies}.
 *
 * @author Stephane Nicoll
 */
class XmppDestinationPoliciesTests {

	@Test
	void allowedJabberIdsAcceptsListedJabberId() {
		assertThat(accept("replies@example.org", "xmpp:replies@example.org")).isTrue();
	}

	@Test
	void allowedJabberIdsRejectsOtherDomain() {
		assertThat(accept("replies@example.org", "xmpp:replies@evil.net")).isFalse();
	}

	@Test
	void allowedJabberIdsRejectsOtherLocalPart() {
		assertThat(accept("replies@example.org", "xmpp:everyone@example.org")).isFalse();
	}

	@Test
	void allowedJabberIdsRejectsSubDomain() {
		assertThat(accept("replies@example.org", "xmpp:replies@evil.example.org")).isFalse();
	}

	@Test
	void allowedJabberIdsRejectsResourceOnAnAllowedBareJabberId() {
		assertThat(accept("replies@example.org", "xmpp:replies@example.org/mobile")).isFalse();
	}

	@Test
	void allowedJabberIdsRejectsDifferentCase() {
		assertThat(accept("replies@example.org", "xmpp:Replies@Example.ORG")).isFalse();
	}

	@Test
	void allowedJabberIdsDoesNotConstrainApplicationDestinations() {
		DestinationPolicy<XmppDestinationDescriptor> policy = XmppDestinationPolicies
			.allowedJabberIds("replies@example.org");
		assertThat(
				policy.accept(XmppDestinationDescriptor.of(URI.create("xmpp:user@other.net"), UriSource.APPLICATION)))
			.isTrue();
	}

	@Test
	void allowedJabberIdsRejectsEmptyJabberIds() {
		assertThatIllegalArgumentException().isThrownBy(XmppDestinationPolicies::allowedJabberIds)
			.withMessageContaining("'jabberIds' must not be empty");
	}

	@Test
	void allowedJabberIdsRejectsEmptyJabberId() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> XmppDestinationPolicies.allowedJabberIds(Collections.singletonList(" ")))
			.withMessageContaining("'jabberIds' must not contain an empty Jabber ID");
	}

	@Test
	void allowedJabberIdsRejectsJabberIdWithWhitespace() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> XmppDestinationPolicies.allowedJabberIds("replies@example.org x"))
			.withMessageContaining("must not contain whitespace or a control character");
	}

	private boolean accept(String allowedJabberId, String uri) {
		return XmppDestinationPolicies.allowedJabberIds(allowedJabberId)
			.accept(XmppDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE));
	}

}
