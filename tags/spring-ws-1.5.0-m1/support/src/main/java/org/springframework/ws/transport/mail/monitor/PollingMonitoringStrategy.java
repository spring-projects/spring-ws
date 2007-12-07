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

package org.springframework.ws.transport.mail.monitor;

import javax.mail.Folder;
import javax.mail.MessagingException;

/**
 * Implementation of the {@link MonitoringStrategy} interface that uses a simple polling mechanism. Defines a {@link
 * #setPollingInterval(long) polling interval} property which defines the interval in between message polls.
 * <p/>
 * <b>Note</b> that this implementation is not suitable for use with POP3 servers. Use the {@link
 * Pop3PollingMonitoringStrategy} instead.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class PollingMonitoringStrategy extends AbstractMonitoringStrategy {

    /**
     * Defines the default polling frequency. Set to 1000 * 60 milliseconds (i.e. 1 minute).
     */
    public static final long DEFAULT_POLLING_FREQUENCY = 1000 * 60;

    private long pollingInterval = DEFAULT_POLLING_FREQUENCY;

    /**
     * Sets the interval used in between message polls, <strong>in milliseconds</strong>. The default is 1000 * 60 ms,
     * that is 1 minute.
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    protected void waitForNewMessages(Folder folder) throws MessagingException, InterruptedException {
        Thread.sleep(pollingInterval);
        afterSleep(folder);
    }

    /**
     * Invoked after the {@link Thread#sleep(long)} method has been invoked. This implementation calls {@link
     * Folder#getMessageCount(), to force new messages to be seen.
     *
     * @param folder the folder to check for new messages
     * @throws MessagingException in case of JavaMail errors
     */
    protected void afterSleep(Folder folder) throws MessagingException {
        folder.getMessageCount();
    }

}
