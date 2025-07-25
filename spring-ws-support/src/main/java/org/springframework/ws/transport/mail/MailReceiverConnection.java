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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.Header;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.internet.InternetAddress;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for server-side Mail
 * access. Exposes a {@link Message} request and response message.
 *
 * @author Arjen Poutsma
 * @author Greg Turnquist
 * @since 1.5.0
 */
public class MailReceiverConnection extends AbstractReceiverConnection {

	private final Message requestMessage;

	private final Session session;

	private @Nullable Message responseMessage;

	private @Nullable ByteArrayOutputStream responseBuffer;

	private @Nullable String responseContentType;

	private @Nullable URLName transportUri;

	private @Nullable InternetAddress from;

	/** Constructs a new Mail connection with the given parameters. */
	protected MailReceiverConnection(Message requestMessage, Session session) {
		Assert.notNull(requestMessage, "'requestMessage' must not be null");
		Assert.notNull(session, "'session' must not be null");
		this.requestMessage = requestMessage;
		this.session = session;
	}

	/** Returns the request message for this connection. */
	public Message getRequestMessage() {
		return this.requestMessage;
	}

	/** Returns the response message, if any, for this connection. */
	public Message getResponseMessage() {
		Assert.notNull(this.responseMessage, "ResponseMessage is not available");
		return this.responseMessage;
	}

	/*
	 * Package-friendly setters
	 */
	void setTransportUri(URLName transportUri) {
		this.transportUri = transportUri;
	}

	void setFrom(InternetAddress from) {
		this.from = from;
	}

	/*
	 * URI
	 */

	@Override
	public URI getUri() throws URISyntaxException {
		try {
			Address[] recipients = this.requestMessage.getRecipients(Message.RecipientType.TO);
			if (!ObjectUtils.isEmpty(recipients) && recipients[0] instanceof InternetAddress) {
				return MailTransportUtils.toUri((InternetAddress) recipients[0], this.requestMessage.getSubject());
			}
			else {
				throw new URISyntaxException("", "Could not determine To header");
			}
		}
		catch (MessagingException ex) {
			throw new URISyntaxException("", ex.getMessage());
		}
	}
	/*
	 * Errors
	 */

	@Override
	public @Nullable String getErrorMessage() throws IOException {
		return null;
	}

	@Override
	public boolean hasError() throws IOException {
		return false;
	}

	/*
	 * Receiving
	 */

	@Override
	public Iterator<String> getRequestHeaderNames() throws IOException {
		try {
			List<String> headers = new ArrayList<>();
			Enumeration<?> enumeration = this.requestMessage.getAllHeaders();
			while (enumeration.hasMoreElements()) {
				Header header = (Header) enumeration.nextElement();
				headers.add(header.getName());
			}
			return headers.iterator();
		}
		catch (MessagingException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	@Override
	public Iterator<String> getRequestHeaders(String name) throws IOException {
		try {
			String[] headers = this.requestMessage.getHeader(name);
			return Arrays.asList(headers).iterator();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	protected InputStream getRequestInputStream() throws IOException {
		try {
			return this.requestMessage.getInputStream();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	public void addResponseHeader(String name, String value) throws IOException {
		try {
			getResponseMessage().addHeader(name, value);
			if (TransportConstants.HEADER_CONTENT_TYPE.equals(name)) {
				this.responseContentType = value;
			}
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	protected OutputStream getResponseOutputStream() throws IOException {
		Assert.state(this.responseBuffer != null, "onSendBeforeWrite has not been called");
		return this.responseBuffer;
	}

	/*
	 * Sending
	 */

	@Override
	protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
		try {
			this.responseMessage = this.requestMessage.reply(false);
			this.responseMessage.setFrom(this.from);

			this.responseBuffer = new ByteArrayOutputStream();
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
	}

	@Override
	protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
		Transport transport = null;
		Assert.state(this.responseMessage != null, "onSendAfterWrite has not been called");
		Assert.state(this.responseBuffer != null, "onSendAfterWrite has not been called");
		Assert.notNull(this.transportUri, "'transportUri' must not be null");
		try {
			this.responseMessage.setDataHandler(new DataHandler(
					new ByteArrayDataSource(this.responseBuffer.toByteArray(), this.responseContentType)));
			transport = this.session.getTransport(this.transportUri);
			transport.connect();
			this.responseMessage.saveChanges();
			transport.sendMessage(this.responseMessage, this.responseMessage.getAllRecipients());
		}
		catch (MessagingException ex) {
			throw new MailTransportException(ex);
		}
		finally {
			MailTransportUtils.closeService(transport);
		}
	}

	private static final class ByteArrayDataSource implements DataSource {

		private final String contentType;

		private final byte[] data;

		ByteArrayDataSource(byte[] data, @Nullable String contentType) {
			this.data = data;
			this.contentType = (contentType != null) ? contentType : "application/octet-stream";
		}

		@Override
		public String getContentType() {
			return this.contentType;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(this.data);
		}

		@Override
		public String getName() {
			return "ByteArrayDataSource";
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

	}

}
