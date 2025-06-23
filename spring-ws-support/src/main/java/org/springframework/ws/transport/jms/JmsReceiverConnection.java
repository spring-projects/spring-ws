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

package org.springframework.ws.transport.jms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.jspecify.annotations.Nullable;

import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for server-side JMS access.
 * Exposes a {@link BytesMessage} or {@link TextMessage} request and response message.
 * <p>
 * The response message type is equal to the request message type, i.e. if a
 * {@code BytesMessage} is received as request, a {@code BytesMessage} is created as
 * response, and if a {@code TextMessage} is received, a {@code TextMessage} response is
 * created.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */
public class JmsReceiverConnection extends AbstractReceiverConnection {

	private final Message requestMessage;

	private final Session session;

	private @Nullable Message responseMessage;

	private @Nullable String textMessageEncoding;

	private @Nullable MessagePostProcessor postProcessor;

	private JmsReceiverConnection(Message requestMessage, Session session) {
		Assert.notNull(requestMessage, "requestMessage must not be null");
		Assert.notNull(session, "session must not be null");
		this.requestMessage = requestMessage;
		this.session = session;
	}

	/**
	 * Constructs a new JMS connection with the given {@link BytesMessage}.
	 * @param requestMessage the JMS request message
	 * @param session the JMS session
	 */
	protected JmsReceiverConnection(BytesMessage requestMessage, Session session) {
		this((Message) requestMessage, session);
	}

	/**
	 * Constructs a new JMS connection with the given {@link TextMessage}.
	 * @param requestMessage the JMS request message
	 * @param session the JMS session
	 */
	protected JmsReceiverConnection(TextMessage requestMessage, String encoding, Session session) {
		this(requestMessage, session);
		this.textMessageEncoding = encoding;
	}

	void setPostProcessor(@Nullable MessagePostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	/**
	 * Returns the request message for this connection. Returns either a
	 * {@link BytesMessage} or a {@link TextMessage}.
	 */
	public Message getRequestMessage() {
		return this.requestMessage;
	}

	/**
	 * Returns the response message, if any, for this connection. Returns either a
	 * {@link BytesMessage} or a {@link TextMessage}.
	 */
	public @Nullable Message getResponseMessage() {
		return this.responseMessage;
	}

	/*
	 * URI
	 */

	@Override
	public @Nullable URI getUri() throws URISyntaxException {
		try {
			return JmsTransportUtils.toUri(this.requestMessage.getJMSDestination());
		}
		catch (JMSException ex) {
			throw new URISyntaxException("", ex.getMessage());
		}
	}

	/*
	 * Errors
	 */

	@Override
	public @Nullable String getErrorMessage() throws IOException {
		return null;
	}

	@Override
	public boolean hasError() throws IOException {
		return false;
	}

	/*
	 * Receiving
	 */

	@Override
	public Iterator<String> getRequestHeaderNames() throws IOException {
		try {
			return JmsTransportUtils.getHeaderNames(this.requestMessage);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not get property names", ex);
		}
	}

	@Override
	public Iterator<String> getRequestHeaders(String name) throws IOException {
		try {
			return JmsTransportUtils.getHeaders(this.requestMessage, name);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not get property value", ex);
		}
	}

	@Override
	protected InputStream getRequestInputStream() throws IOException {
		if (this.requestMessage instanceof BytesMessage) {
			return new BytesMessageInputStream((BytesMessage) this.requestMessage);
		}
		else if (this.requestMessage instanceof TextMessage) {
			Assert.notNull(this.textMessageEncoding, "MessageEncoding for TextMessage is required");
			return new TextMessageInputStream((TextMessage) this.requestMessage, this.textMessageEncoding);
		}
		else {
			throw new IllegalStateException("Unknown request message type [" + this.requestMessage + "]");
		}
	}

	/*
	 * Sending
	 */

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		try {
			this.responseMessage = createResponseMessage();
			String correlation = this.requestMessage.getJMSCorrelationID();
			if (correlation == null) {
				correlation = this.requestMessage.getJMSMessageID();
			}
			this.responseMessage.setJMSCorrelationID(correlation);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not create response message", ex);
		}
	}

	private Message createResponseMessage() throws JMSException {
		if (this.requestMessage instanceof BytesMessage) {
			return this.session.createBytesMessage();
		}
		else if (this.requestMessage instanceof TextMessage) {
			return this.session.createTextMessage();
		}
		else {
			throw new IllegalStateException("Unknown request message type [" + this.requestMessage + "]");
		}
	}

	@Override
	public void addResponseHeader(String name, String value) throws IOException {
		Assert.state(this.responseMessage != null, "Response message is not available");
		try {
			JmsTransportUtils.addHeader(this.responseMessage, name, value);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not set property", ex);
		}
	}

	@Override
	protected OutputStream getResponseOutputStream() throws IOException {
		if (this.responseMessage instanceof BytesMessage) {
			return new BytesMessageOutputStream((BytesMessage) this.responseMessage);
		}
		else if (this.responseMessage instanceof TextMessage) {
			Assert.notNull(this.textMessageEncoding, "MessageEncoding for TextMessage is required");
			return new TextMessageOutputStream((TextMessage) this.responseMessage, this.textMessageEncoding);
		}
		else {
			throw new IllegalStateException("Unknown response message type [" + this.responseMessage + "]");
		}
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		Assert.state(this.responseMessage != null, "Response message is not available");
		MessageProducer messageProducer = null;
		try {
			if (this.requestMessage.getJMSReplyTo() != null) {
				messageProducer = this.session.createProducer(this.requestMessage.getJMSReplyTo());
				messageProducer.setDeliveryMode(this.requestMessage.getJMSDeliveryMode());
				messageProducer.setPriority(this.requestMessage.getJMSPriority());
				if (this.postProcessor != null) {
					this.responseMessage = this.postProcessor.postProcessMessage(this.responseMessage);
				}
				messageProducer.send(this.responseMessage);
			}
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
		finally {
			JmsUtils.closeMessageProducer(messageProducer);
		}
	}

}
