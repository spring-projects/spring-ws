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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

/**
 * {@link DestinationPolicy DestinationPolicy} implementations for mail destinations.
 * <p>
 * A policy is required for a sender to accept a {@linkplain UriSource#REMOTE remote}
 * destination, such as a WS-Addressing {@code ReplyTo} or {@code FaultTo} address:
 *
 * <pre class="code">
 * MailMessageSender sender = new MailMessageSender();
 * sender.setDestinationPolicy(MailDestinationPolicies.allowedRecipients("replies@partner.example.com"));
 * </pre>
 *
 * @author Stephane Nicoll
 * @since 3.1.10
 */
public abstract class MailDestinationPolicies {

	/**
	 * Return a policy that accepts a remote destination only if its mailbox is one of the
	 * given addresses, and accepts any application-supplied destination.
	 * @param addresses the mailboxes to accept
	 * @return a policy accepting only those mailboxes for remote destinations
	 * @see #allowedRecipients(Collection)
	 */
	public static DestinationPolicy<MailDestinationDescriptor> allowedRecipients(String... addresses) {
		Assert.notNull(addresses, "'addresses' must not be null");
		return allowedRecipients(Arrays.asList(addresses));
	}

	/**
	 * Return a policy that accepts a remote destination only if its mailbox is one of the
	 * given addresses, and accepts any application-supplied destination.
	 * <p>
	 * Both sides of the comparison are the {@linkplain InternetAddress#getAddress()
	 * address} of a mailbox parsed by JavaMail, so what is compared is the mailbox that
	 * the message would be sent to. The comparison is exact, including the case of the
	 * local part, which only the receiving mail system can declare insignificant. A
	 * mailbox that differs in any way, including one carrying anything beyond an address,
	 * therefore does not match a configured recipient.
	 * <p>
	 * Each configured address must be a single well-formed mailbox, as parsed in
	 * {@linkplain InternetAddress#InternetAddress(String, boolean) strict} mode.
	 * @param addresses the mailboxes to accept
	 * @return a policy accepting only those mailboxes for remote destinations
	 * @throws IllegalArgumentException if no address is given, or an address is not a
	 * well-formed mailbox
	 */
	public static DestinationPolicy<MailDestinationDescriptor> allowedRecipients(Collection<String> addresses) {
		Set<String> allowedRecipients = validate(addresses);
		return DestinationPolicy.forRemoteDestinations((descriptor) -> {
			InternetAddress toAddress = descriptor.toAddress();
			return toAddress != null && allowedRecipients.contains(toAddress.getAddress());
		});
	}

	private static Set<String> validate(Collection<String> addresses) {
		Assert.notEmpty(addresses, "'addresses' must not be empty");
		Set<String> allowedRecipients = new LinkedHashSet<>();
		for (String address : addresses) {
			Assert.hasText(address, "'addresses' must not contain an empty address");
			allowedRecipients.add(parse(address));
		}
		return allowedRecipients;
	}

	private static String parse(String address) {
		try {
			String parsed = new InternetAddress(address, true).getAddress();
			Assert.isTrue(parsed != null && parsed.equals(address),
					() -> "'" + address + "' must be a single mailbox with nothing around it");
			return parsed;
		}
		catch (AddressException ex) {
			throw new IllegalArgumentException("'" + address + "' is not a well-formed mailbox", ex);
		}
	}

}
