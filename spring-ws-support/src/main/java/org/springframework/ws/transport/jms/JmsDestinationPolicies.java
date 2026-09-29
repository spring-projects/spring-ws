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

package org.springframework.ws.transport.jms;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

/**
 * {@link DestinationPolicy DestinationPolicy} implementations for JMS destinations.
 * <p>
 * A policy is required for a sender to accept a {@linkplain UriSource#REMOTE remote}
 * destination, such as a WS-Addressing {@code ReplyTo} or {@code FaultTo} address:
 *
 * <pre class="code">
 * JmsMessageSender sender = new JmsMessageSender(connectionFactory);
 * sender.setDestinationPolicy(JmsDestinationPolicies.allowedDestinationNames("ReplyQueue"));
 * </pre>
 *
 * @author Stephane Nicoll
 * @since 3.1.10
 */
public abstract class JmsDestinationPolicies {

	/**
	 * Return a policy that accepts a remote destination only if both its destination name
	 * and its {@code replyToName}, when present, match one of the given names, and
	 * accepts any application-supplied destination.
	 * <p>
	 * Names are compared exactly, including case, as a JMS destination name is opaque and
	 * only the provider can declare two names equal. Both names are checked because the
	 * sender hands both to its destination resolver, which treats either as a lookup key
	 * when it resolves through JNDI.
	 * @param names the destination names to accept
	 * @return a policy accepting only those names for remote destinations
	 */
	public static DestinationPolicy<JmsDestinationDescriptor> allowedDestinationNames(String... names) {
		Assert.notNull(names, "'names' must not be null");
		return allowedDestinationNames(Arrays.asList(names));
	}

	/**
	 * Return a policy that accepts a remote destination only if both its destination name
	 * and its {@code replyToName}, when present, match one of the given names, and
	 * accepts any application-supplied destination.
	 * <p>
	 * Names are compared exactly, including case, as a JMS destination name is opaque and
	 * only the provider can declare two names equal. Both names are checked because the
	 * sender hands both to its destination resolver, which treats either as a lookup key
	 * when it resolves through JNDI.
	 * @param names the destination names to accept
	 * @return a policy accepting only those names for remote destinations
	 */
	public static DestinationPolicy<JmsDestinationDescriptor> allowedDestinationNames(Collection<String> names) {
		Assert.notEmpty(names, "'names' must not be empty");
		Set<String> allowedNames = new LinkedHashSet<>();
		for (String name : names) {
			Assert.hasText(name, "'names' must not contain an empty name");
			allowedNames.add(name);
		}
		return DestinationPolicy.forRemoteDestinations((descriptor) -> {
			if (!allowedNames.contains(descriptor.destinationName())) {
				return false;
			}
			String replyToName = descriptor.replyToName();
			return !StringUtils.hasLength(replyToName) || allowedNames.contains(replyToName);
		});
	}

}
