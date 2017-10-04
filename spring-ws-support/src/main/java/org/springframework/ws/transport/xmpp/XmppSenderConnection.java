/*
 * Copyright 2005-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.filter.ThreadFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * Implementation of {@link org.springframework.ws.transport.WebServiceConnection} that is used for client-side XMPP
 * access. Exposes a {@link Message} request and response message.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 2.0
 */
public class XmppSenderConnection extends AbstractSenderConnection {

	private final Message requestMessage;

	private final XMPPConnection connection;

	private Message responseMessage;

	private String messageEncoding;

	private long receiveTimeout;

	protected XmppSenderConnection(XMPPConnection connection, String to, String thread) {
		Assert.notNull(connection, "'connection' must not be null");
		Assert.hasLength(to, "'to' must not be empty");
		Assert.hasLength(thread, "'thread' must not be empty");
		this.connection = connection;
		try {
			this.requestMessage = new Message(JidCreate.from(to), Message.Type.chat);
		} catch (XmppStringprepException e) {
			throw new RuntimeException(e);
		}
		this.requestMessage.setThread(thread);
	}

	/** Returns the request message for this connection. */
	public Message getRequestMessage() {
		return requestMessage;
	}

	/** Returns the response message, if any, for this connection. */
	public Message getResponseMessage() {
		return responseMessage;
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
		return XmppTransportUtils.toUri(requestMessage);
	}

	/*
	 * Errors
	 */

	@Override
	public boolean hasError() {
		return XmppTransportUtils.hasError(responseMessage);
	}

	@Override
	public String getErrorMessage() {
		return XmppTransportUtils.getErrorMessage(responseMessage);
	}

	/*
	 * Sending
	 */

	@Override
	public void addRequestHeader(String name, String value) {
		XmppTransportUtils.addHeader(requestMessage, name, value);
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return new MessageOutputStream(requestMessage, messageEncoding);
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		requestMessage.setFrom(connection.getUser());
		try {
			connection.sendStanza(requestMessage);
		} catch (SmackException.NotConnectedException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	/*
	 * Receiving
	 */

	@Override
	protected void onReceiveBeforeRead() throws IOException {
		StanzaFilter packetFilter = createPacketFilter();

		StanzaCollector collector = connection.createStanzaCollector(packetFilter);
		try {
			Stanza packet = receiveTimeout >= 0 ? collector.nextResult(receiveTimeout) : collector.nextResult();
			if (packet instanceof Message) {
				responseMessage = (Message) packet;
			} else if (packet != null) {
				throw new IllegalArgumentException(
						"Wrong packet type: [" + packet.getClass() + "]. Only Messages can be handled.");
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	private StanzaFilter createPacketFilter() {
		AndFilter andFilter = new AndFilter();
		andFilter.addFilter(new StanzaTypeFilter(Message.class));
		andFilter.addFilter(new ThreadFilter(requestMessage.getThread()));
		return andFilter;
	}

	@Override
	protected boolean hasResponse() throws IOException {
		return responseMessage != null;
	}

	@Override
	public Iterator<String> getResponseHeaderNames() {
		return XmppTransportUtils.getHeaderNames(responseMessage);
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		return XmppTransportUtils.getHeaders(responseMessage, name);
	}

	@Override
	protected InputStream getResponseInputStream() throws IOException {
		return new MessageInputStream(responseMessage, messageEncoding);
	}

}
