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

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.util.Assert;
import org.springframework.ws.transport.mail.support.MailUtils;
import org.springframework.ws.transport.support.AbstractMultiThreadedMessageReceiver;

/**
 * @author Arjen Poutsma
 */
public class MailMessageReceiver extends AbstractMultiThreadedMessageReceiver {

    private Session session = Session.getInstance(new Properties(), null);

    private URLName storeUri;

    private URLName transportUri;

    private Folder folder;

    private Store store;

    private MonitoringStrategy monitoringStrategy = new DefaultMonitoringStrategy();

    private InternetAddress from;

    public void setFrom(String from) throws AddressException {
        this.from = new InternetAddress(from);
    }

    /**
     * Set JavaMail properties for the {@link Session}.
     * <p/>
     * A new {@link Session} will be created with those properties. Use either this method or {@link #setSession}, but
     * not both.
     * <p/>
     * Non-default properties in this instance will override given JavaMail properties.
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        session = Session.getInstance(javaMailProperties, null);
    }

    /**
     *
     * @param monitoringStrategy
     */
    public void setMonitoringStrategy(MonitoringStrategy monitoringStrategy) {
        this.monitoringStrategy = monitoringStrategy;
    }

    /**
     * Set the JavaMail <code>Session</code>, possibly pulled from JNDI.
     * <p/>
     * Default is a new <code>Session</code> without defaults, that is completely configured via this instance's
     * properties.
     * <p/>
     * If using a pre-configured <code>Session</code>, non-default properties in this instance will override the
     * settings in the <code>Session</code>.
     *
     * @see #setJavaMailProperties
     */
    public void setSession(Session session) {
        Assert.notNull(session, "Session must not be null");
        this.session = session;
    }

    public void setStoreUri(String storeUri) {
        this.storeUri = new URLName(storeUri);
    }

    public void setTransportUri(String transportUri) {
        this.transportUri = new URLName(transportUri);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(storeUri, "Property 'storeUri' is required");
        Assert.notNull(transportUri, "Property 'transportUri' is required");
        Assert.notNull(monitoringStrategy, "Property 'monitoringStrategy' is required");
        super.afterPropertiesSet();
    }

    protected void onActivate() throws Exception {
        openFolder();
    }

    protected void onStart() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting mail receiver [" + storeUri.toString() + "]");
        }
        getTaskExecutor().execute(new MonitoringRunnable());
    }

    protected void onStop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping mail receiver [" + storeUri.toString() + "]");
        }
    }

    protected void onShutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("Shutting down mail receiver [" + storeUri.toString() + "]");
        }
        closeFolder();
    }

    protected void closeFolder() {
        MailUtils.closeFolder(folder, true);
        MailUtils.closeService(store);
    }

    protected void openFolder() throws MessagingException, MailTransportException {
        store = session.getStore(storeUri);
        store.connect();
        folder = store.getFolder(storeUri);
        if (folder == null || !folder.exists()) {
            throw new MailTransportException("No default folder to receive from");
        }
        folder.open(Folder.READ_WRITE);
    }

    private class MonitoringRunnable implements Runnable {

        public void run() {
            while (isRunning()) {
                try {
                    Message[] newMessages = monitoringStrategy.getNewMessages(folder);
                    for (int i = 0; i < newMessages.length; i++) {
                        if (logger.isDebugEnabled()) {
                            if (newMessages[i] instanceof MimeMessage) {
                                MimeMessage mimeMessage = (MimeMessage) newMessages[i];
                                logger.debug("Received email message with MessageID " + mimeMessage.getMessageID());
                            }
                        }
                        MessageRequestHandler handler = new MessageRequestHandler(newMessages[i]);
                        getTaskExecutor().execute(handler);
                    }
                }
                catch (MessagingException ex) {
                    logger.warn(ex);
                }
            }
        }
    }

    private class MessageRequestHandler implements Runnable {

        private final Message message;

        public MessageRequestHandler(Message message) {
            this.message = message;
        }

        public void run() {
            MailReceiverConnection connection = new MailReceiverConnection(message, session);
            connection.setTransportUri(transportUri);
            connection.setFrom(from);
            try {
                handleConnection(connection);
            }
            catch (Exception ex) {
                logger.warn("Could not handle message", ex);
            }
        }
    }
}
