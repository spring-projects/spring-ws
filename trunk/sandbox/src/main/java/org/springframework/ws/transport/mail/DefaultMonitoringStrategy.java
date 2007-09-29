/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.transport.mail;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

/**
 * Default implementation of the {@link MonitoringStrategy}. Polls for new messages using a defined {@link
 * #setPollingInterval(int) interval}.
 *
 * @author Arjen Poutsma
 */
public class DefaultMonitoringStrategy extends AbstractPollingMonitoringStrategy {

    private boolean deleteMessages = true;

    /**
     * Sets whether messages should be marked as {@link Flags.Flag#DELETED DELETED} after they have been read. Default
     * is <code>true</code>.
     */
    public void setDeleteMessages(boolean deleteMessages) {
        this.deleteMessages = deleteMessages;
    }

    /**
     * Polls for new messages in the given folder. Calls {@link #createSearchTerm(Folder)}, and uses that created term
     * to search for messages in the given folder. Marks the messages as  {@link Flags.Flag#DELETED DELETED} if the
     * {@link #setDeleteMessages(boolean) deleteMessages} property is set.
     */
    protected final Message[] pollForNewMessages(Folder folder) throws MessagingException {
        SearchTerm searchTerm = createSearchTerm(folder);
        Message[] messages;
        if (searchTerm == null) {
            messages = folder.getMessages();
        }
        else {
            messages = folder.search(searchTerm);
        }
        if (messages.length > 0) {
            FetchProfile contentsProfile = new FetchProfile();
            contentsProfile.add(FetchProfile.Item.ENVELOPE);
            contentsProfile.add(FetchProfile.Item.CONTENT_INFO);
            folder.fetch(messages, contentsProfile);
            if (deleteMessages) {
                for (int i = 0; i < messages.length; i++) {
                    messages[i].setFlag(Flags.Flag.DELETED, true);
                }
            }
        }
        return messages;
    }

    /**
     * Creates the search term that defines the messages to look for. Default implementation returns a term that
     * searches for all messages in the folder that are {@link Flags.Flag#RECENT RECENT}, not {@link Flags.Flag#ANSWERED
     * ANSWERED}, and not {@link Flags.Flag#DELETED DELETED}.
     * <p/>
     * Return <code>null</code> if all messages should be returned from {@link #pollForNewMessages(Folder)}.
     */
    protected SearchTerm createSearchTerm(Folder folder) {
        Flags supportedFlags = folder.getPermanentFlags();
        SearchTerm searchTerm = null;
        if (supportedFlags.contains(Flags.Flag.RECENT)) {
            searchTerm = new FlagTerm(new Flags(Flags.Flag.RECENT), true);
        }
        if (supportedFlags.contains(Flags.Flag.ANSWERED)) {
            FlagTerm answeredTerm = new FlagTerm(new Flags(Flags.Flag.ANSWERED), false);
            if (searchTerm == null) {
                searchTerm = answeredTerm;
            }
            else {
                searchTerm = new AndTerm(searchTerm, answeredTerm);
            }
        }
        if (supportedFlags.contains(Flags.Flag.DELETED)) {
            FlagTerm deletedTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
            if (searchTerm == null) {
                searchTerm = deletedTerm;
            }
            else {
                searchTerm = new AndTerm(searchTerm, deletedTerm);
            }
        }
        return searchTerm;
    }

}
