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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Objects;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.filter.ThreadFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.Stanza;
import org.jspecify.annotations.Nullable;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * Implementation of {@link org.springframework.ws.transport.WebServiceConnection} that is
 * used for client-side XMPP access. Exposes a {@link Message} request and response
 * message.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 2.0
 */
public class XmppSenderConnection extends AbstractSenderConnection {

	private final MessageBuilder requestMessageBuilder;

	private final XMPPConnection connection;

	private @Nullable MessageBuilder responseMessageBuilder;

	private String messageEncoding = XmppMessageReceiver.DEFAULT_MESSAGE_ENCODING;

	private long receiveTimeout;

	protected XmppSenderConnection(XMPPConnection connection, String to, String thread) {
		Assert.notNull(connection, "'connection' must not be null");
		Assert.hasLength(to, "'to' must not be empty");
		Assert.hasLength(thread, "'thread' must not be empty");
		this.connection = connection;
		try {
			this.requestMessageBuilder = connection.getStanzaFactory()
				.buildMessageStanza()
				.to(JidCreate.from(to))
				.ofType(Message.Type.chat)
				.setThread(thread);
		}
		catch (XmppStringprepException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Returns the request message for this connection. */
	public Message getRequestMessage() {
		return this.requestMessageBuilder.build();
	}

	/** Returns the response message, if any, for this connection. */
	public @Nullable Message getResponseMessage() {
		return (this.responseMessageBuilder != null) ? this.responseMessageBuilder.build() : null;
	}

	/*
	 * Package-friendly setters
	 */

	void setMessageEncoding(String messageEncoding) {
		this.messageEncoding = messageEncoding;
	}

	void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/*
	 * URI
	 */

	@Override
	public URI getUri() throws URISyntaxException {
		return XmppTransportUtils.toUri(this.requestMessageBuilder);
	}

	/*
	 * Errors
	 */

	@Override
	public boolean hasError() {
		return XmppTransportUtils.hasError(this.responseMessageBuilder);
	}

	@Override
	public @Nullable String getErrorMessage() {
		return XmppTransportUtils.getErrorMessage(this.responseMessageBuilder);
	}

	/*
	 * Sending
	 */

	@Override
	public void addRequestHeader(String name, String value) {
		XmppTransportUtils.addHeader(this.requestMessageBuilder, name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return new MessageOutputStream(this.requestMessageBuilder, this.messageEncoding);
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		this.requestMessageBuilder.from(this.connection.getUser());
		try {
			this.connection.sendStanza(this.requestMessageBuilder.build());
		}
		catch (SmackException.NotConnectedException | InterruptedException ex) {
			throw new IOException(ex);
		}
	}

	/*
	 * Receiving
	 */

	@Override
	protected void onReceiveBeforeRead() throws IOException {
		StanzaFilter packetFilter = createPacketFilter();

		StanzaCollector collector = this.connection.createStanzaCollector(packetFilter);
		try {
			Stanza packet = (this.receiveTimeout >= 0) ? collector.nextResult(this.receiveTimeout)
					: collector.nextResult();
			if (packet instanceof Message message) {
				this.responseMessageBuilder = this.connection.getStanzaFactory().buildMessageStanzaFrom(message);
			}
			else if (packet != null) {
				throw new IllegalArgumentException(
						"Wrong packet type: [" + packet.getClass() + "]. Only Messages can be handled.");
			}
		}
		catch (InterruptedException ex) {
			throw new IOException(ex);
		}
	}

	private StanzaFilter createPacketFilter() {
		AndFilter andFilter = new AndFilter();
		andFilter.addFilter(new StanzaTypeFilter(Message.class));
		andFilter.addFilter(new ThreadFilter(this.requestMessageBuilder.getThread()));
		return andFilter;
	}

	@Override
	protected boolean hasResponse() throws IOException {
		return this.responseMessageBuilder != null;
	}

	@Override
	public Iterator<String> getResponseHeaderNames() {
		return XmppTransportUtils.getHeaderNames(Objects.requireNonNull(this.responseMessageBuilder).build());
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		return XmppTransportUtils.getHeaders(Objects.requireNonNull(this.responseMessageBuilder).build(), name);
	}

	@Override
	protected InputStream getResponseInputStream() throws IOException {
		return new MessageInputStream(Objects.requireNonNull(this.responseMessageBuilder).build(),
				this.messageEncoding);
	}

}
