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

package org.springframework.ws.transport.mail;

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
 * Tests for {@link MailDestinationPolicies}.
 *
 * @author Stephane Nicoll
 */
class MailDestinationPoliciesTests {

	@Test
	void allowedRecipientsAcceptsListedMailbox() {
		assertThat(accept("replies@partner.example.com", "mailto:replies@partner.example.com")).isTrue();
	}

	@Test
	void allowedRecipientsKeepsSubjectOutOfTheDecision() {
		assertThat(accept("replies@partner.example.com", "mailto:replies@partner.example.com?subject=Anything"))
			.isTrue();
	}

	@Test
	void allowedRecipientsRejectsOtherMailbox() {
		assertThat(accept("replies@partner.example.com", "mailto:attacker@evil.example.net")).isFalse();
	}

	@Test
	void allowedRecipientsRejectsOtherMailboxOnTheSameHost() {
		assertThat(accept("replies@partner.example.com", "mailto:everyone@partner.example.com")).isFalse();
	}

	@Test
	void allowedRecipientsRejectsDifferentCase() {
		assertThat(accept("replies@partner.example.com", "mailto:Replies@partner.example.com")).isFalse();
	}

	@Test
	void allowedRecipientsRejectsMailboxWithoutHost() {
		assertThat(accept("replies@partner.example.com", "mailto:replies")).isFalse();
	}

	@Test
	void allowedRecipientsRejectsUnparsableMailbox() {
		assertThat(accept("replies@partner.example.com", "mailto:@partner.example.com")).isFalse();
	}

	@Test
	void allowedRecipientsRejectsHeaderInjectionAroundAnAllowedMailbox() {
		assertThat(accept("replies@partner.example.com",
				"mailto:replies@partner.example.com%0D%0ABcc:attacker@evil.example.net"))
			.isFalse();
	}

	@Test
	void allowedRecipientsDoesNotConstrainApplicationDestinations() {
		DestinationPolicy<MailDestinationDescriptor> policy = MailDestinationPolicies
			.allowedRecipients("replies@partner.example.com");
		assertThat(policy
			.accept(MailDestinationDescriptor.of(URI.create("mailto:anyone@other.example"), UriSource.APPLICATION)))
			.isTrue();
	}

	@Test
	void allowedRecipientsRejectsEmptyAddresses() {
		assertThatIllegalArgumentException().isThrownBy(MailDestinationPolicies::allowedRecipients)
			.withMessageContaining("'addresses' must not be empty");
	}

	@Test
	void allowedRecipientsRejectsEmptyAddress() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> MailDestinationPolicies.allowedRecipients(Collections.singletonList(" ")))
			.withMessageContaining("'addresses' must not contain an empty address");
	}

	@ParameterizedTest
	@ValueSource(strings = { "not a mailbox", "replies@partner.example.com, other@example.com" })
	void allowedRecipientsRejectsAddressThatIsNotAWellFormedMailbox(String address) {
		assertThatIllegalArgumentException().isThrownBy(() -> MailDestinationPolicies.allowedRecipients(address))
			.withMessageContaining("is not a well-formed mailbox");
	}

	@Test
	void allowedRecipientsRejectsAddressWithSurroundingText() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> MailDestinationPolicies.allowedRecipients("Replies <replies@partner.example.com>"))
			.withMessageContaining("must be a single mailbox with nothing around it");
	}

	private boolean accept(String allowedRecipient, String uri) {
		return MailDestinationPolicies.allowedRecipients(allowedRecipient)
			.accept(MailDestinationDescriptor.of(URI.create(uri), UriSource.REMOTE));
	}

}
