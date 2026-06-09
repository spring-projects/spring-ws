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

import java.net.URI;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * {@link DestinationDescriptor} implementation for JMS destinations.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class JmsDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private JmsDestinationDescriptor(URI uri, UriSource uriSource) {
		this.uri = uri;
		this.uriSource = uriSource;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static JmsDestinationDescriptor of(URI uri, UriSource uriSource) {
		return new JmsDestinationDescriptor(uri, uriSource);
	}

	@Override
	public URI uri() {
		return this.uri;
	}

	@Override
	public UriSource uriSource() {
		return this.uriSource;
	}

	/**
	 * Return the name of the destination.
	 * @return the destination name
	 */
	public @Nullable String destinationName() {
		return JmsTransportUtils.getDestinationName(this.uri);
	}

	/**
	 * Return the name of the replyTo.
	 * @return the replyTo name
	 */
	public @Nullable String replyToName() {
		return JmsTransportUtils.getReplyToName(this.uri);
	}

	/**
	 * Check if either the destination name or the replyTo name are suspicious, that is if
	 * either of those names contains a known protocol that could lead to RCE attacks.
	 * @return {@code true} if any of the destination or replyTo names is suspicious,
	 * {@code false} otherwise
	 */
	public boolean containsSuspiciousJmsNameIndirection() {
		return containsSuspiciousToken(destinationName()) || containsSuspiciousToken(replyToName());
	}

	private static boolean containsSuspiciousToken(@Nullable String name) {
		if (!StringUtils.hasLength(name)) {
			return false;
		}
		String lower = name.toLowerCase(Locale.ROOT);
		return lower.contains("ldap:") || lower.contains("ldaps:") || lower.contains("rmi:") || lower.contains("dns:")
				|| lower.contains("iiop:") || lower.contains("iiopname:") || lower.contains("corbaname:")
				|| lower.contains("java:") || lower.contains("${");
	}

}
