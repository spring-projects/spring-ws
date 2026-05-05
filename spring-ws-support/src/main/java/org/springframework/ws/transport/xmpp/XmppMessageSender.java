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

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

import org.jivesoftware.smack.XMPPConnection;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.AbstractWebServiceMessageSender;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * {@link WebServiceMessageSender} implementation that uses XMPP
 * {@link org.jivesoftware.smack.packet.Message}s. Requires a
 * {@link #setConnection(org.jivesoftware.smack.XMPPConnection) connection}to be set.
 * <p>
 * This message sender supports URI's of the following format: <blockquote>
 * <tt><b>xmpp:</b></tt><i>to</i> </blockquote> The <i>to</i> represents a Jabber ID.
 * <p>
 * For {@link UriSource#APPLICATION}, default checks accept any {@code xmpp:} URI. For
 * {@link UriSource#REMOTE}, default checks require a configured {@link XMPPConnection}
 * and accept only URIs whose JID domain matches the connection's XMPP service domain (see
 * {@link XmppDestinationDescriptor#domain()}). Use a
 * {@link WebServiceMessageSender.DestinationPolicy} for allow-lists, multi-tenant
 * routing, or stricter JID checks.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 2.0
 */
public class XmppMessageSender extends AbstractWebServiceMessageSender<XmppDestinationDescriptor>
		implements InitializingBean {

	public XmppMessageSender() {
		super((uri) -> uri.getScheme().equals(XmppTransportConstants.XMPP_URI_SCHEME));
	}

	/**
	 * Default timeout for receive operations: -1 indicates a blocking receive without
	 * timeout.
	 */
	public static final long DEFAULT_RECEIVE_TIMEOUT = -1;

	/**
	 * Default encoding used to read from and write to
	 * {@link org.jivesoftware.smack.packet.Message} messages.
	 */
	public static final String DEFAULT_MESSAGE_ENCODING = "UTF-8";

	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

	private String messageEncoding = DEFAULT_MESSAGE_ENCODING;

	private XMPPConnection connection;

	/**
	 * Sets the {@code XMPPConnection}. Setting this property is required.
	 */
	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}

	/**
	 * Set the timeout to use for receive calls. The default is -1, which means no
	 * timeout.
	 * @see org.jivesoftware.smack.StanzaCollector#nextResult(long)
	 */
	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/**
	 * Sets the encoding used to read from {@link org.jivesoftware.smack.packet.Message}
	 * object. Defaults to {@code UTF-8}.
	 */
	public void setMessageEncoding(String messageEncoding) {
		this.messageEncoding = messageEncoding;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.connection, "'connection' is required");
	}

	@Override
	public WebServiceConnection createConnection(URI uri) throws IOException {
		String to = XmppTransportUtils.getTo(uri);
		String thread = createThread();
		XmppSenderConnection connection = new XmppSenderConnection(this.connection, to, thread);
		connection.setReceiveTimeout(this.receiveTimeout);
		connection.setMessageEncoding(this.messageEncoding);
		return connection;
	}

	@Override
	protected XmppDestinationDescriptor createDescriptor(URI uri, WebServiceMessageSender.UriSource uriSource) {
		return XmppDestinationDescriptor.of(uri, uriSource);
	}

	@Override
	protected Predicate<XmppDestinationDescriptor> defaultChecks(WebServiceMessageSender.UriSource uriSource) {
		return (uriSource == UriSource.REMOTE) ? this::xmppRemoteDefaultChecks : (details) -> true;
	}

	private boolean xmppRemoteDefaultChecks(XmppDestinationDescriptor descriptor) {
		if (this.connection == null) {
			return false;
		}
		String serviceDomain = this.connection.getXMPPServiceDomain().toString().toLowerCase(Locale.ROOT);
		if (!StringUtils.hasLength(serviceDomain)) {
			return false;
		}
		return descriptor.domain() != null && serviceDomain.equalsIgnoreCase(descriptor.domain());
	}

	protected String createThread() {
		return UUID.randomUUID().toString();
	}

}
