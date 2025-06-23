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
