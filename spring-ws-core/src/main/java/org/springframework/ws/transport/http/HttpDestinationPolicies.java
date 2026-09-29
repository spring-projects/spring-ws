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

package org.springframework.ws.transport.http;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationPolicy;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

/**
 * {@link DestinationPolicy DestinationPolicy} implementations for HTTP destinations.
 * <p>
 * A policy is required for a sender to accept a {@linkplain UriSource#REMOTE remote}
 * destination, such as a WS-Addressing {@code ReplyTo} or {@code FaultTo} address:
 *
 * <pre class="code">
 * HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
 * sender.setDestinationPolicy(HttpDestinationPolicies.allowedHosts("replies.partner.example.com"));
 * </pre>
 * <p>
 * <b>Note</b>: a policy sees the destination that the sender is about to connect to, and
 * nothing beyond it. HTTP clients that follow redirects can still be steered to another
 * host by an allowed destination, so accept only destinations that are trusted not to
 * redirect elsewhere, or configure the underlying client that the
 * {@link AbstractHttpWebServiceMessageSender} implementation uses to not follow
 * redirects.
 *
 * @author Stephane Nicoll
 * @since 3.1.10
 */
public abstract class HttpDestinationPolicies {

	/**
	 * Return a policy that accepts a remote destination only if its host is one of the
	 * given hosts, and accepts any application-supplied destination.
	 * @param hosts the host names or IPv4 literals to accept
	 * @return a policy accepting only those hosts for remote destinations
	 * @see #allowedHosts(Collection)
	 */
	public static DestinationPolicy<HttpDestinationDescriptor> allowedHosts(String... hosts) {
		Assert.notNull(hosts, "'hosts' must not be null");
		return allowedHosts(Arrays.asList(hosts));
	}

	/**
	 * Return a policy that accepts a remote destination only if its host is one of the
	 * given hosts, and accepts any application-supplied destination.
	 * <p>
	 * A host is compared as written, so that what is compared is what will be connected
	 * to. Each configured host must be a host name made of ASCII letters, digits,
	 * {@code -} and {@code .}. Anything else is rejected here rather than silently never
	 * matching. Use your own policy with IPv6.
	 * @param hosts the host names or IPv4 literals to accept
	 * @return a policy accepting only those hosts for remote destinations
	 */
	public static DestinationPolicy<HttpDestinationDescriptor> allowedHosts(Collection<String> hosts) {
		Set<String> allowedHosts = validate(hosts);
		return DestinationPolicy.forRemoteDestinations((descriptor) -> {
			String host = comparableHost(descriptor.uri());
			return host != null && allowedHosts.contains(host);
		});
	}

	private static Set<String> validate(Collection<String> hosts) {
		Assert.notEmpty(hosts, "'hosts' must not be empty");
		Set<String> allowedHosts = new LinkedHashSet<>();
		for (String host : hosts) {
			Assert.hasText(host, "'hosts' must not contain an empty host");
			Assert.isTrue(isHost(host),
					() -> "'" + host + "' cannot be compared against the host of a destination. Use ASCII letters, "
							+ "digits, '-' and '.'.");
			allowedHosts.add(host);
		}
		return allowedHosts;
	}

	/**
	 * Return the host of the given destination in a form that can be compared with a
	 * configured host, or {@code null} if it has no host that can be compared.
	 */
	private static @Nullable String comparableHost(URI uri) {
		String host = uri.getHost();
		if (host == null) {
			return null;
		}
		String expectedAuthority = (uri.getPort() != -1) ? host + ":" + uri.getPort() : host;
		if (!expectedAuthority.equals(uri.getRawAuthority())) {
			return null;
		}
		return host;
	}

	/**
	 * Whether the given value is a host name or IP literal that can be compared verbatim
	 * with the host of a destination.
	 */
	private static boolean isHost(String candidate) {
		if (candidate.isEmpty()) {
			return false;
		}
		if (candidate.charAt(0) == '.' || candidate.charAt(candidate.length() - 1) == '.') {
			return false;
		}
		for (int i = 0; i < candidate.length(); i++) {
			char c = candidate.charAt(i);
			if (!isAsciiLetterOrDigit(c) && c != '-' && c != '.') {
				return false;
			}
		}
		return true;
	}

	private static boolean isAsciiLetterOrDigit(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
	}

}
