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
import java.util.Locale;

import jakarta.mail.internet.InternetAddress;
import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * {@link DestinationDescriptor} implementation for Mail destinations.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class MailDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private final @Nullable InternetAddress toAddress;

	private final @Nullable String mailboxHost;

	private MailDestinationDescriptor(URI uri, UriSource uriSource, @Nullable InternetAddress toAddress,
			@Nullable String mailboxHost) {
		this.uri = uri;
		this.uriSource = uriSource;
		this.toAddress = toAddress;
		this.mailboxHost = mailboxHost;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static MailDestinationDescriptor of(URI uri, UriSource uriSource) {
		InternetAddress parsedTo = MailTransportUtils.getTo(uri);
		return new MailDestinationDescriptor(uri, uriSource, parsedTo, extractMailboxHost(parsedTo));
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
	 * Return the parsed {@code To} address, or {@code null} when missing or invalid.
	 * @return the destination address, or {@code null}
	 */
	public @Nullable InternetAddress toAddress() {
		return this.toAddress;
	}

	/**
	 * Return the domain part of the mailbox (after {@code @}), lower-cased, or
	 * {@code null} when not extractable.
	 * @return the mailbox host, or {@code null}
	 */
	public @Nullable String mailboxHost() {
		return this.mailboxHost;
	}

	private static @Nullable String extractMailboxHost(@Nullable InternetAddress to) {
		if (to == null) {
			return null;
		}
		String address = to.getAddress();
		if (!StringUtils.hasText(address) || address.length() > 512) {
			return null;
		}
		if (address.indexOf('\n') >= 0 || address.indexOf('\r') >= 0) {
			return null;
		}
		int at = address.lastIndexOf('@');
		if (at <= 0 || at == address.length() - 1) {
			return null;
		}
		String host = address.substring(at + 1).trim().toLowerCase(Locale.ROOT);
		if (!StringUtils.hasLength(host)) {
			return null;
		}
		return host;
	}

}
