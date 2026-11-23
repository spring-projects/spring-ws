/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link JmsDestinationPolicies}.
 *
 * @author Stephane Nicoll
 */
class JmsDestinationPoliciesTests {

	@Test
	void allowedDestinationNamesAcceptsListedName() {
		assertThat(accept(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue"), "jms:ReplyQueue")).isTrue();
	}

	@Test
	void allowedDestinationNamesIsCaseSensitive() {
		assertThat(accept(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue"), "jms:replyqueue")).isFalse();
	}

	@Test
	void allowedDestinationNamesRejectsOtherName() {
		assertThat(accept(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue"), "jms:OtherQueue")).isFalse();
	}

	@Test
	void allowedDestinationNamesAcceptsAllowedReplyToName() {
		assertThat(accept(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue", "AuditQueue"),
				"jms:ReplyQueue?replyToName=AuditQueue"))
			.isTrue();
	}

	@Test
	void allowedDestinationNamesRejectsUnknownReplyToName() {
		assertThat(accept(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue"),
				"jms:ReplyQueue?replyToName=OtherQueue"))
			.isFalse();
	}

	@Test
	void allowedDestinationNamesRejectsIndirectionInReplyToName() {
		assertThat(accept(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue"),
				"jms:ReplyQueue?replyToName=ldap://evil/a"))
			.isFalse();
	}

	@Test
	void allowedDestinationNamesDoesNotConstrainApplicationDestinations() {
		DestinationPolicy<JmsDestinationDescriptor> policy = JmsDestinationPolicies
			.allowedDestinationNames("ReplyQueue");
		assertThat(policy.accept(JmsDestinationDescriptor.of(URI.create("jms:AnyQueue"), UriSource.APPLICATION)))
			.isTrue();
	}

	@Test
	void allowedDestinationNamesRejectsEmptyNames() {
		assertThatIllegalArgumentException().isThrownBy(JmsDestinationPolicies::allowedDestinationNames)
			.withMessageContaining("'names' must not be empty");
	}

	@Test
	void allowedDestinationNamesRejectsEmptyName() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> JmsDestinationPolicies.allowedDestinationNames(Collections.singletonList(" ")))
			.withMessageContaining("'names' must not contain an empty name");
	}

	private boolean accept(DestinationPolicy<JmsDestinationDescriptor> policy, String uri) {
		return policy.accept(JmsDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE));
	}

}
