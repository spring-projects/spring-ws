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

package org.springframework.ws.transport.mail;

import java.util.Objects;
import java.util.Properties;

import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.URLName;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.jspecify.annotations.Nullable;

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
 * Server-side component for receiving email messages using JavaMail. Requires a
 * {@link #setTransportUri(String) transport} URI, {@link #setStoreUri(String) store} URI,
 * and {@link #setMonitoringStrategy(MonitoringStrategy) monitoringStrategy} to be set, in
 * addition to the {@link #setMessageFactory(WebServiceMessageFactory) messageFactory} and
 * {@link #setMessageReceiver(WebServiceMessageReceiver) messageReceiver} required by the
 * base class.
 * <p>
 * The {@link MonitoringStrategy} is used to detect new incoming email request. If the
 * {@code monitoringStrategy} is not explicitly set, this receiver will use the
 * {@link Pop3PollingMonitoringStrategy} for POP3 servers, and the
 * {@link PollingMonitoringStrategy} for IMAP servers.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class MailMessageReceiver extends AbstractAsyncStandaloneMessageReceiver {

	private Session session = Session.getInstance(new Properties(), null);

	@SuppressWarnings("NullAway.Init")
	private URLName storeUri;

	@SuppressWarnings("NullAway.Init")
	private URLName transportUri;

	@SuppressWarnings("NullAway.Init")
	private MonitoringStrategy monitoringStrategy;

	private @Nullable Folder folder;

	private @Nullable Store store;

	private @Nullable InternetAddress from;

	/** Sets the from address to use when sending response messages. */
	public void setFrom(String from) throws AddressException {
		this.from = new InternetAddress(from);
	}

	/**
	 * Set JavaMail properties for the {@link Session}.
	 * <p>
	 * A new {@link Session} will be created with those properties. Use either this method
	 * or {@link #setSession}, but not both.
	 * <p>
	 * Non-default properties in this instance will override given JavaMail properties.
	 */
	public void setJavaMailProperties(Properties javaMailProperties) {
		this.session = Session.getInstance(javaMailProperties, null);
	}

	/**
	 * Set the JavaMail {@code Session}, possibly pulled from JNDI.
	 * <p>
	 * Default is a new {@code Session} without defaults, that is completely configured
	 * via this instance's properties.
	 * <p>
	 * If using a pre-configured {@code Session}, non-default properties in this instance
	 * will override the settings in the {@code Session}.
	 * @see #setJavaMailProperties
	 */
	public void setSession(Session session) {
		Assert.notNull(session, "Session must not be null");
		this.session = session;
	}

	/**
	 * Sets the JavaMail Store URI to be used for retrieving request messages. Typically
	 * takes the form of {@code [imap|pop3]://user:password@host:port/INBOX}. Setting this
	 * property is required.
	 * <p>
	 * For example, {@code imap://john:secret@imap.example.com/INBOX}
	 * @see Session#getStore(URLName)
	 */
	public void setStoreUri(String storeUri) {
		this.storeUri = new URLName(storeUri);
	}

	/**
	 * Sets the JavaMail Transport URI to be used for sending response messages. Typically
	 * takes the form of {@code smtp://user:password@host:port}. Setting this property is
	 * required.
	 * <p>
	 * For example, {@code smtp://john:secret@smtp.example.com}
	 * @see Session#getTransport(URLName)
	 */
	public void setTransportUri(String transportUri) {
		this.transportUri = new URLName(transportUri);
	}

	/**
	 * Sets the monitoring strategy to use for retrieving new requests. Default is the
	 * {@link PollingMonitoringStrategy}.
	 */
	public void setMonitoringStrategy(MonitoringStrategy monitoringStrategy) {
		this.monitoringStrategy = monitoringStrategy;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.storeUri, "Property 'storeUri' is required");
		Assert.notNull(this.transportUri, "Property 'transportUri' is required");
		if (this.monitoringStrategy == null) {
			String protocol = this.storeUri.getProtocol();
			if ("pop3".equals(protocol)) {
				this.monitoringStrategy = new Pop3PollingMonitoringStrategy();
			}
			else if ("imap".equals(protocol)) {
				this.monitoringStrategy = new PollingMonitoringStrategy();
			}
			else {
				throw new IllegalArgumentException("Cannot determine monitoring strategy for \"" + protocol + "\". "
						+ "Set the 'monitoringStrategy' explicitly.");
			}
		}
		super.afterPropertiesSet();
	}

	@Override
	protected void onActivate() throws MessagingException {
		openSession();
		openFolder();
	}

	@Override
	protected void onStart() {
		if (this.logger.isInfoEnabled()) {
			this.logger
				.info("Starting mail receiver [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
		}
		execute(new MonitoringRunnable());
	}

	@Override
	protected void onStop() {
		if (this.logger.isInfoEnabled()) {
			this.logger
				.info("Stopping mail receiver [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
		}
		closeFolder();
	}

	@Override
	protected void onShutdown() {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Shutting down mail receiver ["
					+ MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
		}
		closeFolder();
		closeSession();
	}

	private void openSession() throws MessagingException {
		this.store = this.session.getStore(this.storeUri);
		if (this.logger.isDebugEnabled()) {
			this.logger
				.debug("Connecting to store [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
		}
		this.store.connect();
	}

	private void openFolder() throws MessagingException {
		if (this.folder != null && this.folder.isOpen()) {
			return;
		}
		this.folder = Objects.requireNonNull(this.store).getFolder(this.storeUri);
		if (this.folder == null || !this.folder.exists()) {
			throw new IllegalStateException("No default folder to receive from");
		}
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Opening folder [" + MailTransportUtils.toPasswordProtectedString(this.storeUri) + "]");
		}
		this.folder.open(this.monitoringStrategy.getFolderOpenMode());
	}

	private void closeFolder() {
		MailTransportUtils.closeFolder(this.folder, true);
	}

	private void closeSession() {
		MailTransportUtils.closeService(this.store);
	}

	private final class MonitoringRunnable implements SchedulingAwareRunnable {

		@Override
		public void run() {
			try {
				openFolder();
				while (isRunning()) {
					try {
						Message[] messages = MailMessageReceiver.this.monitoringStrategy
							.monitor(Objects.requireNonNull(MailMessageReceiver.this.folder));
						for (Message message : messages) {
							MessageHandler handler = new MessageHandler(message);
							execute(handler);
						}
					}
					catch (FolderClosedException ex) {
						MailMessageReceiver.this.logger.debug("Folder closed, reopening");
						if (isRunning()) {
							openFolder();
						}
					}
					catch (MessagingException ex) {
						MailMessageReceiver.this.logger.warn(ex);
					}
				}
			}
			catch (InterruptedException ex) {
				// Restore the interrupted status
				Thread.currentThread().interrupt();
			}
			catch (MessagingException ex) {
				MailMessageReceiver.this.logger.error(ex);
			}
		}

		@Override
		public boolean isLongLived() {
			return true;
		}

	}

	private final class MessageHandler implements SchedulingAwareRunnable {

		private final Message message;

		MessageHandler(Message message) {
			this.message = message;
		}

		@Override
		public void run() {
			MailReceiverConnection connection = new MailReceiverConnection(this.message,
					MailMessageReceiver.this.session);
			connection.setTransportUri(MailMessageReceiver.this.transportUri);
			connection.setFrom(Objects.requireNonNull(MailMessageReceiver.this.from));
			try {
				handleConnection(connection);
			}
			catch (Exception ex) {
				MailMessageReceiver.this.logger.error("Could not handle incoming mail connection", ex);
			}
		}

		@Override
		public boolean isLongLived() {
			return false;
		}

	}

}
