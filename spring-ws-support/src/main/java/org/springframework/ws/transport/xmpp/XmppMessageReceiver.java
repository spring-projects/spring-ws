/*
 * Copyright 2005-2025 the original author or authors.
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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import org.springframework.ws.transport.support.AbstractStandaloneMessageReceiver;

/**
 * Server-side component for receiving XMPP (Jabber) messages. Requires a
 * {@linkplain #setConnection(XMPPTCPConnection)} connection} to be set, in addition to
 * the {@link #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * messageFactory} and
 * {@link #setMessageReceiver(org.springframework.ws.transport.WebServiceMessageReceiver)
 * messageReceiver} required by the base class.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 2.0
 * @see org.springframework.ws.transport.xmpp.support.XmppConnectionFactoryBean
 */
public class XmppMessageReceiver extends AbstractStandaloneMessageReceiver {

	/**
	 * Default encoding used to read from and write to
	 * {@link org.jivesoftware.smack.packet.Message} messages.
	 */
	public static final String DEFAULT_MESSAGE_ENCODING = "UTF-8";

	private XMPPTCPConnection connection;

	private WebServicePacketListener packetListener;

	private String messageEncoding = DEFAULT_MESSAGE_ENCODING;

	public XmppMessageReceiver() {
	}

	/** Sets the {@code XMPPConnection} to use. Setting this property is required. */
	public void setConnection(XMPPTCPConnection connection) {
		this.connection = connection;
	}

	@Override
	protected void onActivate() throws XMPPException, IOException, SmackException {
		if (!this.connection.isConnected()) {
			try {
				this.connection.connect();
			}
			catch (InterruptedException ex) {
				throw new IOException(ex);
			}
		}
	}

	@Override
	protected void onStart() {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Starting XMPP receiver [" + this.connection.getUser() + "]");
		}
		this.packetListener = new WebServicePacketListener();
		StanzaFilter packetFilter = new StanzaTypeFilter(Message.class);
		this.connection.addAsyncStanzaListener(this.packetListener, packetFilter);
	}

	@Override
	protected void onStop() {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Stopping XMPP receiver [" + this.connection.getUser() + "]");
		}
		this.connection.removeAsyncStanzaListener(this.packetListener);
		this.packetListener = null;
	}

	@Override
	protected void onShutdown() {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Shutting down XMPP receiver [" + this.connection.getUser() + "]");
		}
		if (this.connection.isConnected()) {
			this.connection.disconnect();
		}
	}

	private final class WebServicePacketListener implements StanzaListener {

		@Override
		public void processStanza(Stanza packet) {
			XmppMessageReceiver.this.logger.info("Received " + packet);
			if (packet instanceof Message message) {
				try {
					XmppReceiverConnection wsConnection = new XmppReceiverConnection(
							XmppMessageReceiver.this.connection, message);
					wsConnection.setMessageEncoding(XmppMessageReceiver.this.messageEncoding);
					handleConnection(wsConnection);
				}
				catch (Exception ex) {
					XmppMessageReceiver.this.logger.error(ex);
				}
			}
		}

	}

}
