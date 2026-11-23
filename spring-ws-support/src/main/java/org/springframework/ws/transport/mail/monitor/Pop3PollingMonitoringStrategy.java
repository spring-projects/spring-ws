/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.transport.mail.monitor;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * Implementation of the {@link MonitoringStrategy} interface that uses a simple polling
 * mechanism suitable for POP3 servers. Since POP3 does not have a native mechanism to
 * determine which messages are "new", this implementation simply retrieves all messages
 * in the {@link Folder}, and delete them afterwards. All messages in the POP3 mailbox are
 * therefore, by definition, new.
 * <p>
 * Setting the {@link #setDeleteMessages(boolean) deleteMessages} property is therefore
 * ignored: messages are always deleted.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class Pop3PollingMonitoringStrategy extends PollingMonitoringStrategy {

	public Pop3PollingMonitoringStrategy() {
		super.setDeleteMessages(true);
	}

	@Override
	public void setDeleteMessages(boolean deleteMessages) {
	}

	/**
	 * Re-opens the folder, if it closed.
	 */
	@Override
	protected void afterSleep(Folder folder) throws MessagingException {
		if (!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
		}
	}

	/**
	 * Simply returns {@link Folder#getMessages()}.
	 */
	@Override
	protected Message[] searchForNewMessages(Folder folder) throws MessagingException {
		return folder.getMessages();
	}

	/**
	 * Deletes the given messages from the given folder, and closes it to expunge deleted
	 * messages.
	 * @param folder the folder to delete messages from
	 * @param messages the messages to delete
	 * @throws MessagingException in case of JavaMail errors
	 */
	@Override
	protected void deleteMessages(Folder folder, Message[] messages) throws MessagingException {
		super.deleteMessages(folder, messages);
		// expunge deleted mails, and make sure we've retrieved them before closing the
		// folder
		for (Message message : messages) {
			new MimeMessage((MimeMessage) message);
		}
		MailTransportUtils.closeFolder(folder, true);
	}

}
