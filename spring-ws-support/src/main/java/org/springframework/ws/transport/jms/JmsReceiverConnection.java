/*
 * Copyright 2005-2014 the original author or authors.
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

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.jms.support.JmsTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for server-side JMS access. Exposes a
 * {@link BytesMessage} or {@link TextMessage} request and response message.
 * <p>
 * The response message type is equal to the request message type, i.e. if a {@code BytesMessage} is received as
 * request, a {@code BytesMessage} is created as response, and if a {@code TextMessage} is received, a
 * {@code TextMessage} response is created.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */
public class JmsReceiverConnection extends AbstractReceiverConnection {

	private final Message requestMessage;

	private final Session session;

	private Message responseMessage;

	private String textMessageEncoding;

	private MessagePostProcessor postProcessor;

	private JmsReceiverConnection(Message requestMessage, Session session) {
		Assert.notNull(requestMessage, "requestMessage must not be null");
		Assert.notNull(session, "session must not be null");
		this.requestMessage = requestMessage;
		this.session = session;
	}

	/**
	 * Constructs a new JMS connection with the given {@link BytesMessage}.
	 *
	 * @param requestMessage the JMS request message
	 * @param session the JMS session
	 */
	protected JmsReceiverConnection(BytesMessage requestMessage, Session session) {
		this((Message) requestMessage, session);
	}

	/**
	 * Constructs a new JMS connection with the given {@link TextMessage}.
	 *
	 * @param requestMessage the JMS request message
	 * @param session the JMS session
	 */
	protected JmsReceiverConnection(TextMessage requestMessage, String encoding, Session session) {
		this(requestMessage, session);
		this.textMessageEncoding = encoding;
	}

	void setPostProcessor(MessagePostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	/**
	 * Returns the request message for this connection. Returns either a {@link BytesMessage} or a {@link TextMessage}.
	 */
	public Message getRequestMessage() {
		return requestMessage;
	}

	/**
	 * Returns the response message, if any, for this connection. Returns either a {@link BytesMessage} or a
	 * {@link TextMessage}.
	 */
	public Message getResponseMessage() {
		return responseMessage;
	}

	/*
	 * URI
	 */

	@Override
	public URI getUri() throws URISyntaxException {
		try {
			return JmsTransportUtils.toUri(requestMessage.getJMSDestination());
		} catch (JMSException ex) {
			throw new URISyntaxException("", ex.getMessage());
		}
	}

	/*
	 * Errors
	 */

	@Override
	public String getErrorMessage() throws IOException {
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
			return JmsTransportUtils.getHeaderNames(requestMessage);
		} catch (JMSException ex) {
			throw new JmsTransportException("Could not get property names", ex);
		}
	}

	@Override
	public Iterator<String> getRequestHeaders(String name) throws IOException {
		try {
			return JmsTransportUtils.getHeaders(requestMessage, name);
		} catch (JMSException ex) {
			throw new JmsTransportException("Could not get property value", ex);
		}
	}

	@Override
	protected InputStream getRequestInputStream() throws IOException {
		if (requestMessage instanceof BytesMessage) {
			return new BytesMessageInputStream((BytesMessage) requestMessage);
		} else if (requestMessage instanceof TextMessage) {
			return new TextMessageInputStream((TextMessage) requestMessage, textMessageEncoding);
		} else {
			throw new IllegalStateException("Unknown request message type [" + requestMessage + "]");
		}
	}

	/*
	 * Sending
	 */

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		try {
			if (requestMessage instanceof BytesMessage) {
				responseMessage = session.createBytesMessage();
			} else if (requestMessage instanceof TextMessage) {
				responseMessage = session.createTextMessage();
			} else {
				throw new IllegalStateException("Unknown request message type [" + requestMessage + "]");
			}
			String correlation = requestMessage.getJMSCorrelationID();
			if (correlation == null) {
				correlation = requestMessage.getJMSMessageID();
			}
			responseMessage.setJMSCorrelationID(correlation);
		} catch (JMSException ex) {
			throw new JmsTransportException("Could not create response message", ex);
		}
	}

	@Override
	public void addResponseHeader(String name, String value) throws IOException {
		try {
			JmsTransportUtils.addHeader(responseMessage, name, value);
		} catch (JMSException ex) {
			throw new JmsTransportException("Could not set property", ex);
		}
	}

	@Override
	protected OutputStream getResponseOutputStream() throws IOException {
		if (responseMessage instanceof BytesMessage) {
			return new BytesMessageOutputStream((BytesMessage) responseMessage);
		} else if (responseMessage instanceof TextMessage) {
			return new TextMessageOutputStream((TextMessage) responseMessage, textMessageEncoding);
		} else {
			throw new IllegalStateException("Unknown response message type [" + responseMessage + "]");
		}
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		MessageProducer messageProducer = null;
		try {
			if (requestMessage.getJMSReplyTo() != null) {
				messageProducer = session.createProducer(requestMessage.getJMSReplyTo());
				messageProducer.setDeliveryMode(requestMessage.getJMSDeliveryMode());
				messageProducer.setPriority(requestMessage.getJMSPriority());
				if (postProcessor != null) {
					responseMessage = postProcessor.postProcessMessage(responseMessage);
				}
				messageProducer.send(responseMessage);
			}
		} catch (JMSException ex) {
			throw new JmsTransportException(ex);
		} finally {
			JmsUtils.closeMessageProducer(messageProducer);
		}
	}

}
