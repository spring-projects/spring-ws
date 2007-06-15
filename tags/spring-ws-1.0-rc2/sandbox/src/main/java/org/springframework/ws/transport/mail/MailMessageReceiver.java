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
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

import com.sun.mail.imap.IMAPFolder;
import org.springframework.util.Assert;
import org.springframework.ws.transport.mail.support.MailUtils;
import org.springframework.ws.transport.support.AbstractMultiThreadedMessageReceiver;

/** @author Arjen Poutsma */
public class MailMessageReceiver extends AbstractMultiThreadedMessageReceiver {

    private Session session = Session.getInstance(new Properties(), null);

    private URLName storeUri;

    private Folder folder;

    private MessageCountHandler eventHandler;

    private Store store;

    private boolean supportsIdle;

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

    public void setStoreUri(String storeUri) {
        this.storeUri = new URLName(storeUri);
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(storeUri, "Property 'storeUri' is required");
    }

    protected void onActivate() throws Exception {
        openFolder();
    }

    protected void onStart() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting mail receiver [" + storeUri.toString() + "]");
        }
        eventHandler = new MessageCountHandler();
        folder.addMessageCountListener(eventHandler);
/*
        try {
            if (folder instanceof IMAPFolder) {
                IMAPFolder f = (IMAPFolder) folder;
                logger.debug(folder.isOpen());
                logger.debug("Starting IDLE");
                f.idle();
                logger.debug("IDLE done");
                supportsIdle = true;
            }
        }
        catch (MessagingException mex) {
            supportsIdle = false;
        }
*/
        logger.debug("Support IDLE: " + supportsIdle);
        getTaskExecutor().execute(new MonitoringRunnable());
    }

    protected void onStop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping mail receiver [" + storeUri.toString() + "]");
        }
        if (eventHandler != null) {
            folder.removeMessageCountListener(eventHandler);
            eventHandler = null;
        }
    }

    protected void onShutdown() {
        if (logger.isInfoEnabled()) {
            logger.info("Shutting down mail receiver [" + storeUri.toString() + "]");
        }
        closeFolder();
    }

    protected void openFolder() throws MessagingException, MailTransportException {
        store = session.getStore(storeUri);
        store.connect();
        folder = store.getFolder(storeUri);
        if (folder == null || !folder.exists()) {
            throw new MailTransportException("No default folder to receive from");
        }
        folder.open(Folder.READ_WRITE);
        logger.info("folder contains " + folder.getMessageCount() + " messages");
    }

    protected void closeFolder() {
        MailUtils.closeFolder(folder);
        MailUtils.closeService(store);
    }

    private class MonitoringRunnable implements Runnable {

        public void run() {
            try {
                while (isRunning()) {
                    if (supportsIdle && folder instanceof IMAPFolder) {
                        IMAPFolder f = (IMAPFolder) folder;
                        logger.debug("IDLE starts");
                        f.idle();
                        logger.debug("IDLE done");
                    }
                    else {
                        Thread.sleep(500); // sleep for freq milliseconds

                        // This is to force the IMAP server to send us
                        // EXISTS notifications.
                        folder.getMessageCount();
                    }
                }
            }
            catch (InterruptedException ex) {
                logger.warn(ex);
            }
            catch (MessagingException ex) {
                logger.warn(ex);
            }
        }
    }

    private class MessageCountHandler implements MessageCountListener {

        public void messagesAdded(MessageCountEvent event) {
            Message[] msgs = event.getMessages();
            logger.info("Got " + msgs.length + " new messages");
        }

        public void messagesRemoved(MessageCountEvent e) {
        }
    }
}
