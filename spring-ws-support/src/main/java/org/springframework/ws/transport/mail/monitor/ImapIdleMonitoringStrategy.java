/*
 * Copyright 2005-2010 the original author or authors.
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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import org.springframework.util.Assert;

import com.sun.mail.imap.IMAPFolder;

/**
 * Implementation of the {@link MonitoringStrategy} interface that uses the IMAP IDLE command for asynchronous message
 * detection.
 * <p>
 * <b>Note</b> that this implementation is only suitable for use with IMAP servers which support the IDLE command.
 * Additionally, this strategy requires JavaMail version 1.4.1.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class ImapIdleMonitoringStrategy extends AbstractMonitoringStrategy {

	private MessageCountListener messageCountListener;

	@Override
	protected void waitForNewMessages(Folder folder) throws MessagingException, InterruptedException {
		Assert.isInstanceOf(IMAPFolder.class, folder);
		IMAPFolder imapFolder = (IMAPFolder) folder;
		// retrieve unseen messages before we enter the blocking idle call
		if (searchForNewMessages(folder).length > 0) {
			return;
		}
		if (messageCountListener == null) {
			createMessageCountListener();
		}
		folder.addMessageCountListener(messageCountListener);
		try {
			imapFolder.idle();
		} finally {
			folder.removeMessageCountListener(messageCountListener);
		}
	}

	private void createMessageCountListener() {
		messageCountListener = new MessageCountAdapter() {
			@Override
			public void messagesAdded(MessageCountEvent e) {
				Message[] messages = e.getMessages();
				for (Message message : messages) {
					try {
						// this will return the flow to the idle call, above
						message.getLineCount();
					} catch (MessagingException ex) {
						// ignore
					}
				}
			}
		};
	}
}
