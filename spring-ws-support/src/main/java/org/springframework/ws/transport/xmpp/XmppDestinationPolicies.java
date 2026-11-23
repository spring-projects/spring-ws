/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

/**
 * {@link DestinationPolicy DestinationPolicy} implementations for XMPP destinations.
 * <p>
 * A policy is required for a sender to accept a {@linkplain UriSource#REMOTE remote}
 * destination, such as a WS-Addressing {@code ReplyTo} or {@code FaultTo} address:
 *
 * <pre class="code">
 * XmppMessageSender sender = new XmppMessageSender();
 * sender.setConnection(connection);
 * sender.setDestinationPolicy(XmppDestinationPolicies.allowedJabberIds("replies@example.org"));
 * </pre>
 *
 * @author Stephane Nicoll
 * @since 3.1.10
 */
public abstract class XmppDestinationPolicies {

	/**
	 * Return a policy that accepts a remote destination only if its Jabber ID is one of
	 * the given Jabber IDs, and accepts any application-supplied destination.
	 * @param jabberIds the Jabber IDs to accept
	 * @return a policy accepting only those Jabber IDs for remote destinations
	 * @see #allowedJabberIds(Collection)
	 */
	public static DestinationPolicy<XmppDestinationDescriptor> allowedJabberIds(String... jabberIds) {
		Assert.notNull(jabberIds, "'jabberIds' must not be null");
		return allowedJabberIds(Arrays.asList(jabberIds));
	}

	/**
	 * Return a policy that accepts a remote destination only if its Jabber ID is one of
	 * the given Jabber IDs, and accepts any application-supplied destination.
	 * <p>
	 * A Jabber ID is compared exactly as the sender would hand it to the XMPP connection,
	 * without normalization of any part of it. A JID that differs in case, or that
	 * carries a resource where none is configured, therefore does not match. List every
	 * form that replies may target, and note that a bare JID is normally what a peer
	 * supplies as a reply address.
	 * @param jabberIds the Jabber IDs to accept
	 * @return a policy accepting only those Jabber IDs for remote destinations
	 * @throws IllegalArgumentException if no Jabber ID is given, or one contains
	 * whitespace or a control character
	 */
	public static DestinationPolicy<XmppDestinationDescriptor> allowedJabberIds(Collection<String> jabberIds) {
		Set<String> allowedJabberIds = validate(jabberIds);
		return DestinationPolicy
			.forRemoteDestinations((descriptor) -> allowedJabberIds.contains(descriptor.jabberId()));
	}

	private static Set<String> validate(Collection<String> jabberIds) {
		Assert.notEmpty(jabberIds, "'jabberIds' must not be empty");
		Set<String> allowedJabberIds = new LinkedHashSet<>();
		for (String jabberId : jabberIds) {
			Assert.hasText(jabberId, "'jabberIds' must not contain an empty Jabber ID");
			Assert.isTrue(hasNoWhitespaceOrControlCharacter(jabberId),
					() -> "'" + jabberId + "' must not contain whitespace or a control character");
			allowedJabberIds.add(jabberId);
		}
		return allowedJabberIds;
	}

	private static boolean hasNoWhitespaceOrControlCharacter(String jabberId) {
		for (int i = 0; i < jabberId.length(); i++) {
			char c = jabberId.charAt(i);
			if (Character.isWhitespace(c) || Character.isISOControl(c)) {
				return false;
			}
		}
		return true;
	}

}
