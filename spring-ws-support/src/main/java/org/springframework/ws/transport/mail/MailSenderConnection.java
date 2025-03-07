/*
 * Copyright 2005-2025 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.HeaderTerm;
import jakarta.mail.search.SearchTerm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for client-side Mail
 * access. Exposes a {@link Message} request and response message.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */

public class MailSenderConnection extends AbstractSenderConnection {

	private static final Log logger = LogFactory.getLog(MailSenderConnection.class);

	private final Session session;

	private MimeMessage requestMessage;

	private Message responseMessage;

	private String requestContentType;

	private boolean deleteAfterReceive = false;

	private final URLName storeUri;

	private final URLName transportUri;

	private ByteArrayOutputStream requestBuffer;

	private InternetAddress from;

	private final InternetAddress to;

	private String subject;

	private final long receiveTimeout;

	private Store store;

	private Folder folder;

	/** Constructs a new Mail connection with the given parameters. */
	protected MailSenderConnection(Session session, URLName transportUri, URLName storeUri, InternetAddress to,
			long receiveTimeout) {
		Assert.notNull(session, "'session' must not be null");
		Assert.notNull(transportUri, "'transportUri' must not be null");
		Assert.notNull(storeUri, "'storeUri' must not be null");
		Assert.notNull(to, "'to' must not be null");
		this.session = session;
		this.transportUri = transportUri;
		this.storeUri = storeUri;
		this.to = to;
		this.receiveTimeout = receiveTimeout;
	}

	/** Returns the request message for this connection. */
	public Message getRequestMessage() {
		return this.requestMessage;
	}

	/** Returns the response message, if any, for this connection. */
	public Message getResponseMessage() {
		return this.responseMessage;
	}

	/*
	 * Package-friendly setters
	 */

	void setFrom(InternetAddress from) {
		this.from = from;
	}

	void setSubject(String subject) {
		this.subject = subject;
	}

	/*
	 * URI
	 */
	@Override
	public URI getUri() throws URISyntaxException {
		return MailTransportUtils.toUri(this.to, this.subject);
	}

	/*
	 * Sending
	 */
	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		try {
			this.requestMessage = new MimeMessage(this.session);
			this.requestMessage.setRecipient(Message.RecipientType.TO, this.to);
			this.requestMessage.setSentDate(new Date());
			if (this.from != null) {
				this.requestMessage.setFrom(this.from);
			}
			if (this.subject != null) {
				this.requestMessage.setSubject(this.subject);
			}
			this.requestBuffer = new ByteArrayOutputStream();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	public void addRequestHeader(String name, String value) throws IOException {
		try {
			this.requestMessage.addHeader(name, value);
			if (TransportConstants.HEADER_CONTENT_TYPE.equals(name)) {
				this.requestContentType = value;
			}
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	protected OutputStream getRequestOutputStream() throws IOException {
		return this.requestBuffer;
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		Transport transport = null;
		try {
			this.requestMessage.setDataHandler(new DataHandler(
					new ByteArrayDataSource(this.requestContentType, this.requestBuffer.toByteArray())));
			transport = this.session.getTransport(this.transportUri);
			transport.connect();
			this.requestMessage.saveChanges();
			transport.sendMessage(this.requestMessage, this.requestMessage.getAllRecipients());
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
		finally {
			MailTransportUtils.closeService(transport);
		}
	}

	/*
	 * Receiving
	 */

	@Override
	protected void onReceiveBeforeRead() throws IOException {
		try {
			String requestMessageId = this.requestMessage.getMessageID();
			Assert.hasLength(requestMessageId, "No Message-ID found on request message [" + this.requestMessage + "]");
			try {
				Thread.sleep(this.receiveTimeout);
			}
			catch (InterruptedException e) {
				// Re-interrupt current thread, to allow other threads to react.
				Thread.currentThread().interrupt();
			}
			openFolder();
			SearchTerm searchTerm = new HeaderTerm(MailTransportConstants.HEADER_IN_REPLY_TO, requestMessageId);
			Message[] responses = this.folder.search(searchTerm);
			if (responses.length > 0) {
				if (responses.length > 1) {
					logger.warn("Received more than one response for request with ID [" + requestMessageId + "]");
				}
				this.responseMessage = responses[0];
			}
			if (this.deleteAfterReceive) {
				this.responseMessage.setFlag(Flags.Flag.DELETED, true);
			}
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	private void openFolder() throws MessagingException {
		this.store = this.session.getStore(this.storeUri);
		this.store.connect();
		this.folder = this.store.getFolder(this.storeUri);
		if (this.folder == null || !this.folder.exists()) {
			throw new IllegalStateException("No default folder to receive from");
		}
		if (this.deleteAfterReceive) {
			this.folder.open(Folder.READ_WRITE);
		}
		else {
			this.folder.open(Folder.READ_ONLY);
		}
	}

	@Override
	protected boolean hasResponse() throws IOException {
		return this.responseMessage != null;
	}

	@Override
	public Iterator<String> getResponseHeaderNames() throws IOException {
		try {
			List<String> headers = new ArrayList<>();
			Enumeration<?> enumeration = this.responseMessage.getAllHeaders();
			while (enumeration.hasMoreElements()) {
				Header header = (Header) enumeration.nextElement();
				headers.add(header.getName());
			}
			return headers.iterator();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	public Iterator<String> getResponseHeaders(String name) throws IOException {
		try {
			String[] headers = this.responseMessage.getHeader(name);
			return Arrays.asList(headers).iterator();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);

		}
	}

	@Override
	protected InputStream getResponseInputStream() throws IOException {
		try {
			return this.responseMessage.getDataHandler().getInputStream();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	public boolean hasError() throws IOException {
		return false;
	}

	@Override
	public String getErrorMessage() throws IOException {
		return null;
	}

	@Override
	public void onClose() throws IOException {
		MailTransportUtils.closeFolder(this.folder, this.deleteAfterReceive);
		MailTransportUtils.closeService(this.store);
	}

	private static class ByteArrayDataSource implements DataSource {

		private byte[] data;

		private String contentType;

		public ByteArrayDataSource(String contentType, byte[] data) {
			this.data = data;
			this.contentType = contentType;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(this.data);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getContentType() {
			return this.contentType;
		}

		@Override
		public String getName() {
			return "ByteArrayDataSource";
		}

	}

}
