/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.xmpp;

import java.net.URI;

import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * {@link DestinationDescriptor} implementation for XMPP destinations.
 * <p>
 * The {@linkplain #jabberId() Jabber ID} is the exact string that the sender hands to the
 * XMPP connection as the recipient of the message, so a policy comparing it decides on
 * the JID that will actually be used.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class XmppDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private final String jabberId;

	private XmppDestinationDescriptor(URI uri, UriSource uriSource, String jabberId) {
		this.uri = uri;
		this.uriSource = uriSource;
		this.jabberId = jabberId;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static XmppDestinationDescriptor of(URI uri, UriSource uriSource) {
		return new XmppDestinationDescriptor(uri, uriSource, XmppTransportUtils.getTo(uri));
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
	 * Return the Jabber ID.
	 * @return the Jabber ID
	 */
	public String jabberId() {
		return this.jabberId;
	}

}
