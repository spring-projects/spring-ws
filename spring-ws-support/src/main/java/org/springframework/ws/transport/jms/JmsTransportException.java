/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.jms;

import jakarta.jms.JMSException;

import org.springframework.ws.transport.TransportException;

/**
 * Exception that is thrown when an error occurs in the JMS transport.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class JmsTransportException extends TransportException {

	private final JMSException jmsException;

	public JmsTransportException(String msg, JMSException ex) {
		super(msg + ": " + ex.getMessage());
		initCause(ex);
		this.jmsException = ex;
	}

	public JmsTransportException(JMSException ex) {
		super(ex.getMessage());
		initCause(ex);
		this.jmsException = ex;
	}

	public JMSException getJmsException() {
		return this.jmsException;
	}

}
