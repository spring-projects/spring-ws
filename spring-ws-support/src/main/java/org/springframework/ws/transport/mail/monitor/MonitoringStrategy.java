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

/**
 * Defines the contract for objects that monitor a given folder for new messages. Allows
 * for multiple implementation strategies, including polling, or event-driven techniques
 * such as IMAP's {@code IDLE} command.
 *
 * @author Arjen Poutsma
 */
public interface MonitoringStrategy {

	/**
	 * Monitors the given folder, and returns any new messages when they arrive.
	 * @param folder the folder in which to look for new messages
	 * @return the new messages
	 * @throws MessagingException in case of JavaMail errors
	 * @throws InterruptedException if a thread is interrupted
	 */
	Message[] monitor(Folder folder) throws MessagingException, InterruptedException;

	/**
	 * Returns the folder open mode to be used by this strategy. Can be either
	 * {@link Folder#READ_ONLY} or {@link Folder#READ_WRITE}.
	 */
	int getFolderOpenMode();

}
