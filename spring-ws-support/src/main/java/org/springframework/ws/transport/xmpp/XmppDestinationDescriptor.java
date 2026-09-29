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
