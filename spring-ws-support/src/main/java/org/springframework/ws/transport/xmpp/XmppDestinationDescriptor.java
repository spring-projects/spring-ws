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
import java.util.Locale;

import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceMessageSender.DestinationDescriptor;
import org.springframework.ws.transport.WebServiceMessageSender.UriSource;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * {@link DestinationDescriptor} implementation for XMPP destinations.
 *
 * @author Stephane Nicoll
 * @since 3.1.9
 */
public final class XmppDestinationDescriptor implements DestinationDescriptor {

	private final URI uri;

	private final UriSource uriSource;

	private final String jabberId;

	private final String domain;

	private XmppDestinationDescriptor(URI uri, UriSource uriSource, String jabberId, String domain) {
		this.uri = uri;
		this.uriSource = uriSource;
		this.jabberId = jabberId;
		this.domain = domain;
	}

	/**
	 * Create an instance from the given URI and URI source.
	 * @param uri the URI
	 * @param uriSource the URI source
	 * @return a descriptor
	 */
	public static XmppDestinationDescriptor of(URI uri, UriSource uriSource) {
		String to = XmppTransportUtils.getTo(uri);
		return new XmppDestinationDescriptor(uri, uriSource, to, extractDomainFromJid(to));
	}

	private static String extractDomainFromJid(String jid) {
		if (!StringUtils.hasLength(jid)) {
			return null;
		}
		int at = jid.indexOf('@');
		if (at < 0 || at == jid.length() - 1) {
			return null;
		}
		String rest = jid.substring(at + 1);
		int slash = rest.indexOf('/');
		if (slash >= 0) {
			rest = rest.substring(0, slash);
		}
		return rest.toLowerCase(Locale.ROOT);
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

	/**
	 * Return the domain part of the JabberID (after {@code @}), lower-cased, or
	 * {@code null}.
	 * @return the domain, or {@code null}
	 */
	public String domain() {
		return this.domain;
	}

}
