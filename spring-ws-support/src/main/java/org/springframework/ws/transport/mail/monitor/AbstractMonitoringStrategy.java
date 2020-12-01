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

package org.springframework.ws.transport.mail.monitor;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for the {@link MonitoringStrategy} interface. Exposes a {@link #setDeleteMessages(boolean)
 * deleteMessages} property, and includes a basic workflow for message monitoring.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractMonitoringStrategy implements MonitoringStrategy {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	private boolean deleteMessages = true;

	/**
	 * Sets whether messages should be marked as {@link javax.mail.Flags.Flag#DELETED DELETED} after they have been read.
	 * Default is {@code true}.
	 */
	public void setDeleteMessages(boolean deleteMessages) {
		this.deleteMessages = deleteMessages;
	}

	@Override
	public int getFolderOpenMode() {
		return deleteMessages ? Folder.READ_WRITE : Folder.READ_ONLY;
	}

	/**
	 * Monitors the given folder, and returns any new messages when they arrive. This implementation calls
	 * {@link #waitForNewMessages(Folder)}, then searches for new messages using {@link #searchForNewMessages(Folder)},
	 * fetches the messages using {@link #fetchMessages(Folder, Message[])}, and finally
	 * {@link #setDeleteMessages(boolean) deletes} the messages, if {@link #setDeleteMessages(boolean) deleteMessages} is
	 * {@code true}.
	 *
	 * @param folder the folder to monitor
	 * @return the new messages
	 * @throws MessagingException in case of JavaMail errors
	 * @throws InterruptedException when a thread is interrupted
	 */
	@Override
	public final Message[] monitor(Folder folder) throws MessagingException, InterruptedException {
		waitForNewMessages(folder);
		Message[] messages = searchForNewMessages(folder);
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + messages.length + " new messages");
		}
		if (messages.length > 0) {
			fetchMessages(folder, messages);
		}
		if (deleteMessages) {
			deleteMessages(folder, messages);
		}
		return messages;
	}

	/**
	 * Template method that blocks until new messages arrive in the given folder. Typical implementations use
	 * {@link Thread#sleep(long)} or the IMAP IDLE command.
	 *
	 * @param folder the folder to monitor
	 * @throws MessagingException in case of JavaMail errors
	 * @throws InterruptedException when a thread is interrupted
	 */
	protected abstract void waitForNewMessages(Folder folder) throws MessagingException, InterruptedException;

	/**
	 * Retrieves new messages from the given folder. This implementation creates a {@link SearchTerm} that searches for
	 * all messages in the folder that are {@link javax.mail.Flags.Flag#RECENT RECENT}, not
	 * {@link javax.mail.Flags.Flag#ANSWERED ANSWERED}, and not {@link javax.mail.Flags.Flag#DELETED DELETED}. The search
	 * term is used to {@link Folder#search(SearchTerm) search} for new messages.
	 *
	 * @param folder the folder to retrieve new messages from
	 * @return the new messages
	 * @throws MessagingException in case of JavaMail errors
	 */
	protected Message[] searchForNewMessages(Folder folder) throws MessagingException {
		if (!folder.isOpen()) {
			return new Message[0];
		}
		Flags supportedFlags = folder.getPermanentFlags();
		SearchTerm searchTerm = null;
		if (supportedFlags != null) {
			if (supportedFlags.contains(Flags.Flag.RECENT)) {
				searchTerm = new FlagTerm(new Flags(Flags.Flag.RECENT), true);
			}
			if (supportedFlags.contains(Flags.Flag.ANSWERED)) {
				FlagTerm answeredTerm = new FlagTerm(new Flags(Flags.Flag.ANSWERED), false);
				if (searchTerm == null) {
					searchTerm = answeredTerm;
				} else {
					searchTerm = new AndTerm(searchTerm, answeredTerm);
				}
			}
			if (supportedFlags.contains(Flags.Flag.DELETED)) {
				FlagTerm deletedTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
				if (searchTerm == null) {
					searchTerm = deletedTerm;
				} else {
					searchTerm = new AndTerm(searchTerm, deletedTerm);
				}
			}
		}
		return searchTerm != null ? folder.search(searchTerm) : folder.getMessages();
	}

	/**
	 * Fetches the specified messages from the specified folder. Default implementation
	 * {@link Folder#fetch(Message[], FetchProfile) fetches} every {@link javax.mail.FetchProfile.Item}.
	 *
	 * @param folder the folder to fetch messages from
	 * @param messages the messages to fetch
	 * @throws MessagingException in case of JavMail errors
	 */
	protected void fetchMessages(Folder folder, Message[] messages) throws MessagingException {
		FetchProfile contentsProfile = new FetchProfile();
		contentsProfile.add(FetchProfile.Item.ENVELOPE);
		contentsProfile.add(FetchProfile.Item.CONTENT_INFO);
		contentsProfile.add(FetchProfile.Item.FLAGS);
		folder.fetch(messages, contentsProfile);
	}

	/**
	 * Deletes the given messages from the given folder. Only invoked when {@link #setDeleteMessages(boolean)} is
	 * {@code true}.
	 *
	 * @param folder the folder to delete messages from
	 * @param messages the messages to delete
	 * @throws MessagingException in case of JavaMail errors
	 */
	protected void deleteMessages(Folder folder, Message[] messages) throws MessagingException {
		for (Message message : messages) {
			message.setFlag(Flags.Flag.DELETED, true);
		}
	}
}
