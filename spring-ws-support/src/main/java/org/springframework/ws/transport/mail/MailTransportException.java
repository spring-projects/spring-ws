/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.mail;

import jakarta.mail.MessagingException;

import org.springframework.ws.transport.TransportException;

/**
 * Exception that is thrown when an error occurs in the Mail transport.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
@SuppressWarnings("serial")
public class MailTransportException extends TransportException {

	private final MessagingException messagingException;

	public MailTransportException(String msg, MessagingException ex) {
		super(msg + ": " + ex.getMessage());
		initCause(ex);
		this.messagingException = ex;
	}

	public MailTransportException(MessagingException ex) {
		super(ex.getMessage());
		initCause(ex);
		this.messagingException = ex;
	}

	public MessagingException getMessagingException() {
		return this.messagingException;
	}

}
