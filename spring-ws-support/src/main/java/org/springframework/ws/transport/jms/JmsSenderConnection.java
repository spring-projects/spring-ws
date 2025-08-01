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
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TextMessage;
import org.jspecify.annotations.Nullable;

import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for client-side JMS access.
 * Exposes a {@link BytesMessage} request and response message.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */
public class JmsSenderConnection extends AbstractSenderConnection {

	private final ConnectionFactory connectionFactory;

	private final Connection connection;

	private final Session session;

	private final Destination requestDestination;

	private Message requestMessage;

	private @Nullable Destination responseDestination;

	private @Nullable Message responseMessage;

	private long receiveTimeout;

	private int deliveryMode;

	private long timeToLive;

	private int priority;

	private @Nullable String textMessageEncoding;

	private @Nullable MessagePostProcessor postProcessor;

	private boolean sessionTransacted = false;

	private boolean temporaryResponseQueueCreated = false;

	/** Constructs a new JMS connection with the given parameters. */
	protected JmsSenderConnection(ConnectionFactory connectionFactory, Connection connection, Session session,
			Destination requestDestination, Message requestMessage) throws JMSException {
		Assert.notNull(connectionFactory, "'connectionFactory' must not be null");
		Assert.notNull(connection, "'connection' must not be null");
		Assert.notNull(session, "'session' must not be null");
		Assert.notNull(requestDestination, "'requestDestination' must not be null");
		Assert.notNull(requestMessage, "'requestMessage' must not be null");
		this.connectionFactory = connectionFactory;
		this.connection = connection;
		this.session = session;
		this.requestDestination = requestDestination;
		this.requestMessage = requestMessage;
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
	 * Package-friendly setters
	 */

	void setResponseDestination(@Nullable Destination responseDestination) {
		this.responseDestination = responseDestination;
	}

	void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	void setPriority(int priority) {
		this.priority = priority;
	}

	void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	void setTextMessageEncoding(String textMessageEncoding) {
		this.textMessageEncoding = textMessageEncoding;
	}

	void setPostProcessor(@Nullable MessagePostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	void setSessionTransacted(boolean sessionTransacted) {
		this.sessionTransacted = sessionTransacted;
	}

	/*
	 * URI
	 */

	@Override
	public @Nullable URI getUri() throws URISyntaxException {
		try {
			return JmsTransportUtils.toUri(this.requestDestination);
		}
		catch (JMSException ex) {
			throw new URISyntaxException("", ex.getMessage());
		}
	}

	/*
	 * Errors
	 */

	@Override
	public boolean hasError() throws IOException {
		return false;
	}

	@Override
	public @Nullable String getErrorMessage() throws IOException {
		return null;
	}

	/*
	 * Sending
	 */

	@Override
	public void addRequestHeader(String name, String value) throws IOException {
		try {
			JmsTransportUtils.addHeader(this.requestMessage, name, value);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not set property", ex);
		}
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		if (this.requestMessage instanceof BytesMessage) {
			return new BytesMessageOutputStream((BytesMessage) this.requestMessage);
		}
		else if (this.requestMessage instanceof TextMessage) {
			Assert.notNull(this.textMessageEncoding, "MessageEncoding for TextMessage is required");
			return new TextMessageOutputStream((TextMessage) this.requestMessage, this.textMessageEncoding);
		}
		else {
			throw new IllegalStateException("Unknown request message type [" + this.requestMessage + "]");
		}

	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		MessageProducer messageProducer = null;
		try {
			messageProducer = this.session.createProducer(this.requestDestination);
			messageProducer.setDeliveryMode(this.deliveryMode);
			messageProducer.setTimeToLive(this.timeToLive);
			messageProducer.setPriority(this.priority);
			if (this.responseDestination == null) {
				this.responseDestination = this.session.createTemporaryQueue();
				this.temporaryResponseQueueCreated = true;
			}
			this.requestMessage.setJMSReplyTo(this.responseDestination);
			if (this.postProcessor != null) {
				this.requestMessage = this.postProcessor.postProcessMessage(this.requestMessage);
			}
			this.connection.start();
			messageProducer.send(this.requestMessage);
			if (this.session.getTransacted() && isSessionLocallyTransacted(this.session)) {
				JmsUtils.commitIfNecessary(this.session);
			}
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
		finally {
			JmsUtils.closeMessageProducer(messageProducer);
		}
	}

	/*
	 * * @see org.springframework.jms.core.JmsTemplate#isSessionLocallyTransacted(Session)
	 * (Session)
	 */
	private boolean isSessionLocallyTransacted(Session session) {
		return this.sessionTransacted
				&& !ConnectionFactoryUtils.isSessionTransactional(session, this.connectionFactory);
	}

	/*
	 * Receiving
	 */

	@Override
	protected void onReceiveBeforeRead() throws IOException {
		MessageConsumer messageConsumer = null;
		try {
			if (this.temporaryResponseQueueCreated) {
				messageConsumer = this.session.createConsumer(this.responseDestination);
			}
			else {
				String messageId = this.requestMessage.getJMSMessageID().replaceAll("'", "''");
				String messageSelector = "JMSCorrelationID = '" + messageId + "'";
				messageConsumer = this.session.createConsumer(this.responseDestination, messageSelector);
			}
			Message message = (this.receiveTimeout >= 0) ? messageConsumer.receive(this.receiveTimeout)
					: messageConsumer.receive();
			if (message instanceof BytesMessage || message instanceof TextMessage) {
				this.responseMessage = message;
			}
			else if (message != null) {
				throw new IllegalArgumentException("Wrong message type: [" + message.getClass() + "]. "
						+ "Only BytesMessages or TextMessages can be handled.");
			}
		}
		catch (JMSException ex) {
			throw new JmsTransportException(ex);
		}
		finally {
			JmsUtils.closeMessageConsumer(messageConsumer);
			if (this.temporaryResponseQueueCreated
					&& this.responseDestination instanceof TemporaryQueue temporaryQueue) {
				try {
					temporaryQueue.delete();
				}
				catch (JMSException ex) {
					// ignore
				}
			}
		}
	}

	@Override
	protected boolean hasResponse() throws IOException {
		return this.responseMessage != null;
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		try {
			Assert.state(this.responseMessage != null, "ResponseMessage is required");
			return JmsTransportUtils.getHeaderNames(this.responseMessage);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not get property names", ex);
		}
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		try {
			Assert.state(this.responseMessage != null, "ResponseMessage is required");
			return JmsTransportUtils.getHeaders(this.responseMessage, name);
		}
		catch (JMSException ex) {
			throw new JmsTransportException("Could not get property value", ex);
		}
	}

	@Override
	protected InputStream getResponseInputStream() throws IOException {
		if (this.responseMessage instanceof BytesMessage) {
			return new BytesMessageInputStream((BytesMessage) this.responseMessage);
		}
		else if (this.responseMessage instanceof TextMessage) {
			Assert.notNull(this.textMessageEncoding, "MessageEncoding for TextMessage is required");
			return new TextMessageInputStream((TextMessage) this.responseMessage, this.textMessageEncoding);
		}
		else {
			throw new IllegalStateException("Unknown response message type [" + this.responseMessage + "]");
		}

	}

	@Override
	protected void onClose() throws IOException {
		JmsUtils.closeSession(this.session);
		ConnectionFactoryUtils.releaseConnection(this.connection, this.connectionFactory, true);
	}

}
