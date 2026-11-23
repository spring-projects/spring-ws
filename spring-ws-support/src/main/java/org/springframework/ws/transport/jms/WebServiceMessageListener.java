/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;

/**
 * Spring {@link SessionAwareMessageListener} that can be used to handle incoming
 * {@link Message} service requests.
 * <p>
 * Requires a {@link WebServiceMessageFactory} which is used to convert the incoming JMS
 * {@link jakarta.jms.BytesMessage} into a {@link WebServiceMessage}, and passes that to
 * the {@link WebServiceMessageReceiver}
 * {@link #setMessageReceiver(WebServiceMessageReceiver) registered}.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 * @see #setMessageFactory(org.springframework.ws.WebServiceMessageFactory)
 * @see #setMessageReceiver(org.springframework.ws.transport.WebServiceMessageReceiver)
 */
public class WebServiceMessageListener extends JmsMessageReceiver implements SessionAwareMessageListener<Message> {

	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		try {
			handleMessage(message, session);
		}
		catch (JmsTransportException ex) {
			throw ex.getJmsException();
		}
		catch (Exception ex) {
			JMSException jmsException = new JMSException(ex.getMessage());
			jmsException.setLinkedException(ex);
			throw jmsException;
		}
	}

}
