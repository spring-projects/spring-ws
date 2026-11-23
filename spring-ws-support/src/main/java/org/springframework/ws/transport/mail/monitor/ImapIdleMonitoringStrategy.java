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
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * Implementation of the {@link MonitoringStrategy} interface that uses the IMAP IDLE
 * command for asynchronous message detection.
 * <p>
 * <b>Note</b> that this implementation is only suitable for use with IMAP servers which
 * support the IDLE command.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class ImapIdleMonitoringStrategy extends AbstractMonitoringStrategy {

	private @Nullable MessageCountListener messageCountListener;

	@Override
	protected void waitForNewMessages(Folder folder) throws MessagingException, InterruptedException {
		Assert.isInstanceOf(IMAPFolder.class, folder);
		IMAPFolder imapFolder = (IMAPFolder) folder;
		// retrieve unseen messages before we enter the blocking idle call
		if (searchForNewMessages(folder).length > 0) {
			return;
		}
		if (this.messageCountListener == null) {
			createMessageCountListener();
		}
		folder.addMessageCountListener(this.messageCountListener);
		try {
			imapFolder.idle();
		}
		finally {
			folder.removeMessageCountListener(this.messageCountListener);
		}
	}

	private void createMessageCountListener() {
		this.messageCountListener = new MessageCountAdapter() {
			@Override
			public void messagesAdded(MessageCountEvent e) {
				Message[] messages = e.getMessages();
				for (Message message : messages) {
					try {
						// this will return the flow to the idle call, above
						message.getLineCount();
					}
					catch (MessagingException ex) {
						// ignore
					}
				}
			}
		};
	}

}
