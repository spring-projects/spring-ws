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
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.transport.WebServiceMessageReceiver;
import org.springframework.ws.transport.mail.monitor.MonitoringStrategy;
import org.springframework.ws.transport.mail.monitor.PollingMonitoringStrategy;
import org.springframework.ws.transport.mail.monitor.Pop3PollingMonitoringStrategy;
import org.springframework.ws.transport.mail.support.MailTransportUtils;
import org.springframework.ws.transport.support.AbstractAsyncStandaloneMessageReceiver;

/**
 * Server-side component for receiving email messages using JavaMail.  Requires a {@link #setTransportUri(String)
 * transport} URI, {@link #setStoreUri(String) store} URI, and {@link #setMonitoringStrategy(MonitoringStrategy)
 * monitoringStrategy} to be set, in addition to the {@link #setMessageFactory(WebServiceMessageFactory) messageFactory}
 * and {@link #setMessageReceiver(WebServiceMessageReceiver) messageReceiver} required by the base class.
 * <p/>
 * The {@link MonitoringStrategy} is used to detect new incoming email request. If the <code>monitoringStrategy</code>
 * is not explicitly set, this receiver will use the {@link Pop3PollingMonitoringStrategy} for POP3 servers, and the
 * {@link PollingMonitoringStrategy} for IMAP servers.
 *
 * @author Arjen Poutsma
 * @since 1.1.0
 */
public class MailMessageReceiver extends AbstractAsyncStandaloneMessageReceiver {

    private Session session = Session.getInstance(new Properties(), null);

    private URLName storeUri;

    private URLName transportUri;

    private Folder folder;

    private Store store;

    private InternetAddress from;

    private MonitoringStrategy monitoringStrategy;

    /** Sets the from address to use when sending reponse messages. */
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

    /**
     * Sets the JavaMail Store URI to be used for retrieving request messages. Typically takes the form of
     * <code>[imap|pop3]://user:password@host:port/INBOX</code>. Setting this property is required.
     * <p/>
     * For example, <code>imap://john:secret@imap.example.com/INBOX</code>
     *
     * @see Session#getStore(URLName)
     */
    public void setStoreUri(String storeUri) {
        this.storeUri = new URLName(storeUri);
    }

    /**
     * Sets the JavaMail Transport URI to be used for sending response messages. Typically takes the form of
     * <code>smtp://user:password@host:port</code>. Setting this property is required.
     * <p/>
     * For example, <code>smtp://john:secret@smtp.example.com</code>
     *
     * @see Session#getTransport(URLName)
     */
    public void setTransportUri(String transportUri) {
        this.transportUri = new URLName(transportUri);
    }

    /**
     * Sets the monitoring strategy to use for retrieving new requests. Default is the {@link
     * PollingMonitoringStrategy}.
     */
    public void setMonitoringStrategy(MonitoringStrategy monitoringStrategy) {
        this.monitoringStrategy = monitoringStrategy;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(storeUri, "Property 'storeUri' is required");
        Assert.notNull(transportUri, "Property 'transportUri' is required");
        if (monitoringStrategy == null) {
            String protocol = storeUri.getProtocol();
            if ("pop3".equals(protocol)) {
                monitoringStrategy = new Pop3PollingMonitoringStrategy();
            }
            else if ("imap".equals(protocol)) {
                monitoringStrategy = new PollingMonitoringStrategy();
            }
            else {
                throw new IllegalArgumentException("Cannot determine monitoring strategy for \"" + protocol + "\". " +
                        "Set the 'monitoringStrategy' explicitly.");
            }
        }
        super.afterPropertiesSet();
    }

    protected void onActivate() throws MessagingException {
        openSession();
        openFolder();
    }

    protected void onStart() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting mail receiver [" + MailTransportUtils.toPasswordProtectedString(storeUri) + "]");
        }
        execute(new MonitoringRunnable());
    }

    protected void onStop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping mail receiver [" + MailTransportUtils.toPasswordProtectedString(storeUri) + "]");
        }
        closeFolder();
    }

    protected void onShutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("Shutting down mail receiver [" + MailTransportUtils.toPasswordProtectedString(storeUri) + "]");
        }
        closeFolder();
        closeSession();
    }

    private void openSession() throws MessagingException {
        store = session.getStore(storeUri);
        if (logger.isDebugEnabled()) {
            logger.debug("Connecting to store [" + MailTransportUtils.toPasswordProtectedString(storeUri) + "]");
        }
        store.connect();
    }

    private void openFolder() throws MessagingException {
        if (folder != null && folder.isOpen()) {
            return;
        }
        folder = store.getFolder(storeUri);
        if (folder == null || !folder.exists()) {
            throw new IllegalStateException("No default folder to receive from");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Opening folder [" + MailTransportUtils.toPasswordProtectedString(storeUri) + "]");
        }
        folder.open(monitoringStrategy.getFolderOpenMode());
    }

    private void closeFolder() {
        MailTransportUtils.closeFolder(folder, true);
    }

    private void closeSession() {
        MailTransportUtils.closeService(store);
    }

    private class MonitoringRunnable implements SchedulingAwareRunnable {

        public void run() {
            try {
                openFolder();
                while (isRunning()) {
                    try {
                        Message[] messages = monitoringStrategy.monitor(folder);
                        for (int i = 0; i < messages.length; i++) {
                            if (logger.isDebugEnabled()) {
                                if (messages[i] instanceof MimeMessage) {
                                    MimeMessage mimeMessage = (MimeMessage) messages[i];
                                    logger.debug("Received email message with MessageID " + mimeMessage.getMessageID());
                                }
                            }
                            MessageHandler handler = new MessageHandler(messages[i]);
                            execute(handler);
                        }
                    }
                    catch (FolderClosedException ex) {
                        logger.debug("Folder closed, reopening");
                        if (isRunning()) {
                            openFolder();
                        }
                    }
                    catch (MessagingException ex) {
                        logger.warn(ex);
                    }
                }
            }
            catch (InterruptedException ex) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
            catch (MessagingException ex) {
                logger.error(ex);
            }
        }

        public boolean isLongLived() {
            return true;
        }
    }

    private class MessageHandler implements SchedulingAwareRunnable {

        private final Message message;

        public MessageHandler(Message message) {
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
                logger.error("Could not handle incoming mail connection", ex);
            }
        }

        public boolean isLongLived() {
            return false;
        }
    }

}
