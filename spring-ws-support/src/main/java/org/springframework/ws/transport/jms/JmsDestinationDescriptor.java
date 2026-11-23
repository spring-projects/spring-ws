/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import java.net.URI;

import org.jspecify.annotations.Nullable;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * {@link DestinationDescriptor} implementation for JMS destinations.
 * <p>
 * The {@linkplain #destinationName() destination name} and {@linkplain #replyToName()
 * replyTo name} are the exact strings that the sender hands to its
 * {@code DestinationResolver}, so a policy comparing them decides on the names that will
 * actually be resolved. Both matter: a resolver that performs a JNDI lookup treats either
 * name as a lookup key.
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

}
