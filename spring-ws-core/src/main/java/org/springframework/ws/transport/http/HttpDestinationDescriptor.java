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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;

/**
 * {@link DestinationDescriptor} implementation for HTTP destinations.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class HttpDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private final Supplier<@Nullable InetAddress> simpleHostAddressResolver = SingletonSupplier
		.of(() -> resolveHostAddress(false));

	private final Supplier<@Nullable InetAddress> dnsResolutionHostAddressResolver = SingletonSupplier
		.of(() -> resolveHostAddress(true));

	private HttpDestinationDescriptor(URI uri, UriSource uriSource) {
		this.uri = uri;
		this.uriSource = uriSource;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static HttpDestinationDescriptor of(URI uri, UriSource uriSource) {
		return new HttpDestinationDescriptor(uri, uriSource);
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
	 * Return the host name of the URI, lower-cased.
	 * @return the host name, or {@code null} if the URI has no host name
	 */
	public @Nullable String hostName() {
		String host = this.uri.getHost();
		if (!StringUtils.hasText(host)) {
			return null;
		}
		String cleanedHostName = host.toLowerCase(Locale.ROOT);
		if (cleanedHostName.length() >= 2 && cleanedHostName.charAt(0) == '['
				&& cleanedHostName.charAt(cleanedHostName.length() - 1) == ']') {
			cleanedHostName = cleanedHostName.substring(1, cleanedHostName.length() - 1);
		}
		return cleanedHostName;
	}

	/**
	 * Resolve the host name to an {@link InetAddress}.
	 * @param allowDnsResolution whether to allow DNS resolution, when false, only IP
	 * literals are considered
	 * @return the address of the host or {@code null} if the host name is not resolvable
	 */
	public @Nullable InetAddress hostAddress(boolean allowDnsResolution) {
		return allowDnsResolution ? this.dnsResolutionHostAddressResolver.get() : this.simpleHostAddressResolver.get();
	}

	/**
	 * Whether the URI host is an IPv4 address literal in a private (RFC&nbsp;1918) range.
	 * @return {@code true} if the host parses as an IPv4 literal and
	 * {@link InetAddress#isSiteLocalAddress()} is {@code true}; {@code false} for host
	 * names, non-literal hosts, non-IPv4 literals, and public IPv4 literals
	 */
	public boolean isSiteLocalIpv4Literal() {
		InetAddress address = hostAddress(false);
		return address instanceof Inet4Address && address.isSiteLocalAddress();
	}

	private @Nullable InetAddress resolveHostAddress(boolean allowDnsResolution) {
		String hostName = hostName();
		if (hostName != null && (allowDnsResolution || isIpLiteral(hostName))) {
			try {
				return InetAddress.getByName(hostName);
			}
			catch (UnknownHostException ex) {
				// do not return loopback
			}
		}
		return null;
	}

	/**
	 * Check if the given host name is an IP literal. {@code true} only for forms
	 * {@link InetAddress#getByName(String)} treats as numeric IP addresses (no DNS),
	 * avoiding resolution for host names.
	 */
	private static boolean isIpLiteral(String hostName) {
		if (hostName.indexOf(':') >= 0) {
			return true;
		}
		if (hostName.indexOf('.') < 0) {
			return false;
		}
		for (int i = 0; i < hostName.length(); i++) {
			char c = hostName.charAt(i);
			if (c != '.' && !Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

}
