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
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.xmpp.support.XmppTransportUtils;

/**
 * Implementation of {@link org.springframework.ws.transport.WebServiceConnection} that is used for server-side XMPP
 * access. Exposes a {@link Message} request and response message.
 *
 * @author Gildas Cuisinier
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 2.0
 */
public class XmppReceiverConnection extends AbstractReceiverConnection {

	private final XMPPConnection connection;

	private final Message requestMessage;

	private Message responseMessage;

	private String messageEncoding;

	public XmppReceiverConnection(XMPPConnection connection, Message requestMessage) {
		Assert.notNull(connection, "'connection' must not be null");
		Assert.notNull(requestMessage, "'requestMessage' must not be null");
		this.connection = connection;
		this.requestMessage = requestMessage;
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
	 * Receiving
	 */

	@Override
	public Iterator<String> getRequestHeaderNames() throws IOException {
		return XmppTransportUtils.getHeaderNames(requestMessage);
	}

	@Override
	public Iterator<String> getRequestHeaders(String name) throws IOException {
		return XmppTransportUtils.getHeaders(requestMessage, name);
	}

	@Override
	protected InputStream getRequestInputStream() throws IOException {
		return new MessageInputStream(requestMessage, messageEncoding);
	}

	/*
	 * Sending
	 */

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		responseMessage = new Message(requestMessage.getFrom(), Message.Type.chat);
		responseMessage.setFrom(connection.getUser());
		responseMessage.setThread(requestMessage.getThread());
	}

	@Override
	public void addResponseHeader(String name, String value) throws IOException {
		XmppTransportUtils.addHeader(responseMessage, name, value);
	}

	@Override
	protected OutputStream getResponseOutputStream() throws IOException {
		return new MessageOutputStream(responseMessage, messageEncoding);
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		try {
			connection.sendStanza(responseMessage);
		} catch (SmackException.NotConnectedException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
}
