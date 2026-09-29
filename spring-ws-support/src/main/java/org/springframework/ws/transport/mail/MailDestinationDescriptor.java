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

import jakarta.mail.internet.InternetAddress;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * {@link DestinationDescriptor} implementation for Mail destinations.
 * <p>
 * The {@linkplain #toAddress() To address} is the very {@link InternetAddress} that the
 * sender sets as the recipient of the message, so a policy comparing it decides on the
 * mailbox that will actually be used.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class MailDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private final @Nullable InternetAddress toAddress;

	private MailDestinationDescriptor(URI uri, UriSource uriSource, @Nullable InternetAddress toAddress) {
		this.uri = uri;
		this.uriSource = uriSource;
		this.toAddress = toAddress;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static MailDestinationDescriptor of(URI uri, UriSource uriSource) {
		return new MailDestinationDescriptor(uri, uriSource, MailTransportUtils.getTo(uri));
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

}
