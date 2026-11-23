/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
