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

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract base class for {@link MonitoringStrategy} implementations that use a polling mechanism. Defines a {@link
 * #setPollingInterval(int) polling interval} property which defines the interval in between message polls.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractPollingMonitoringStrategy implements MonitoringStrategy, InitializingBean {

    /**
     * Defines the default polling frequency. Set to 1000 * 60 * 5 milliseconds (i.e. 5 minutes).
     */
    public static final int DEFAULT_POLLING_FREQUENCY = 1000 * 60 * 5;

    /**
     * Logger available to subclasses.
     */
    private final Log logger = LogFactory.getLog(getClass());

    private int pollingInterval = DEFAULT_POLLING_FREQUENCY;

    public void afterPropertiesSet() throws Exception {
        logger.info("Polling every " + getPollingInterval() + " milliseconds");
    }

    /**
     * Returns the polling interval.
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the interval used in between message polls, <strong>in milliseconds</strong>. The default is 1000 * 60 * 5
     * ms, that is 5 minutes.
     */
    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    /**
     * Sleeps for the {@link #setPollingInterval(int) defined amount of milliseconds}, and calls {@link
     * #pollForNewMessages(Folder)}.
     *
     * @param folder the folder to look in
     * @return the new messages
     * @throws MessagingException in case of JavaMail errors.
     */
    public final Message[] getNewMessages(Folder folder) throws MessagingException {
        try {
            Thread.sleep(getPollingInterval());
            folder.getMessageCount();
            return pollForNewMessages(folder);
        }
        catch (InterruptedException e) {
            logger.warn(e);
            return new Message[0];
        }
    }

    /**
     * Abstract template method that is invoked every interval.
     */
    protected abstract Message[] pollForNewMessages(Folder folder) throws MessagingException;
}
