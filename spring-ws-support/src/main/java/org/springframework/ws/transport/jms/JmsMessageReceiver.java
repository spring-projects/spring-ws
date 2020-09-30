/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.jms;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.support.SimpleWebServiceMessageReceiverObjectSupport;

/**
 * Convenience base class for JMS server-side transport objects. Contains a {@link WebServiceMessageReceiver}, and has
 * methods for handling incoming JMS {@link BytesMessage} and {@link TextMessage} requests. Also contains a
 * {@code textMessageEncoding} property, which determines the encoding used to read from and write to
 * {@code TextMessages}. This property defaults to {@code UTF-8}.
 * <p>
 * Used by {@link WebServiceMessageListener}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class JmsMessageReceiver extends SimpleWebServiceMessageReceiverObjectSupport {

	/** Default encoding used to read from and write to {@link TextMessage} messages. */
	public static final String DEFAULT_TEXT_MESSAGE_ENCODING = "UTF-8";

	private String textMessageEncoding = DEFAULT_TEXT_MESSAGE_ENCODING;

	private MessagePostProcessor postProcessor;

	/** Sets the encoding used to read from and write to {@link TextMessage} messages. Defaults to {@code UTF-8}. */
	public void setTextMessageEncoding(String textMessageEncoding) {
		this.textMessageEncoding = textMessageEncoding;
	}

	/**
	 * Sets the optional {@link MessagePostProcessor} to further modify outgoing messages after the XML contents has been
	 * set.
	 */
	public void setPostProcessor(MessagePostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	/**
	 * Handles an incoming message. Uses the given session to create a response message.
	 *
	 * @param request the incoming message
	 * @param session the JMS session used to create a response
	 * @throws IllegalArgumentException when request is not a {@link BytesMessage}
	 */
	protected final void handleMessage(Message request, Session session) throws Exception {
		JmsReceiverConnection connection;
		if (request instanceof BytesMessage) {
			connection = new JmsReceiverConnection((BytesMessage) request, session);
		} else if (request instanceof TextMessage) {
			connection = new JmsReceiverConnection((TextMessage) request, textMessageEncoding, session);
		} else {
			throw new IllegalArgumentException(
					"Wrong message type: [" + request.getClass() + "]. Only BytesMessages or TextMessages can be handled.");
		}
		connection.setPostProcessor(postProcessor);

		handleConnection(connection);
	}
}
