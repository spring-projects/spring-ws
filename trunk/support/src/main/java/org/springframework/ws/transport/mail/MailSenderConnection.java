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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.HeaderTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for client-side Mail access. Exposes a {@link Message}
 * request and response message.
 *
 * @author Arjen Poutsma
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

    /**
     * Constructs a new Mail connection with the given parameters.
     */
    protected MailSenderConnection(Session session,
                                   URLName transportUri,
                                   URLName storeUri,
                                   InternetAddress to,
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

    /**
     * Returns the request message for this connection.
     */
    public Message getRequestMessage() {
        return requestMessage;
    }

    /**
     * Returns the response message, if any, for this connection.
     */
    public Message getResponseMessage() {
        return responseMessage;
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
    * Sending
    */
    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        try {
            requestMessage = new MimeMessage(session);
            requestMessage.setRecipient(Message.RecipientType.TO, to);
            requestMessage.setSentDate(new Date());
            if (from != null) {
                requestMessage.setFrom(from);
            }
            if (subject != null) {
                requestMessage.setSubject(subject);
            }
            requestBuffer = new ByteArrayOutputStream();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    protected void addRequestHeader(String name, String value) throws IOException {
        try {
            requestMessage.addHeader(name, value);
            if (TransportConstants.HEADER_CONTENT_TYPE.equals(name)) {
                requestContentType = value;
            }
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        return requestBuffer;
    }

    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        Transport transport = null;
        try {
            requestMessage.setDataHandler(
                    new DataHandler(new ByteArrayDataSource(requestContentType, requestBuffer.toByteArray())));
            transport = session.getTransport(transportUri);
            transport.connect();
            requestMessage.saveChanges();
            transport.sendMessage(requestMessage, requestMessage.getAllRecipients());
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

    protected void onReceiveBeforeRead() throws IOException {
        try {
            String requestMessageId = requestMessage.getMessageID();
            Assert.hasLength(requestMessageId, "No Message-ID found on request message [" + requestMessage + "]");
            try {
                Thread.sleep(receiveTimeout);
            }
            catch (InterruptedException e) {
                // Re-interrupt current thread, to allow other threads to react.
                Thread.currentThread().interrupt();
            }
            openFolder();
            SearchTerm searchTerm = new HeaderTerm(MailTransportConstants.HEADER_IN_REPLY_TO, requestMessageId);
            Message[] responses = folder.search(searchTerm);
            if (responses.length > 0) {
                if (responses.length > 1) {
                    logger.warn("Received more than one response for request with ID [" + requestMessageId + "]");
                }
                responseMessage = responses[0];
            }
            if (deleteAfterReceive) {
                responseMessage.setFlag(Flags.Flag.DELETED, true);
            }
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    private void openFolder() throws MessagingException {
        store = session.getStore(storeUri);
        store.connect();
        folder = store.getFolder(storeUri);
        if (folder == null || !folder.exists()) {
            throw new IllegalStateException("No default folder to receive from");
        }
        if (deleteAfterReceive) {
            folder.open(Folder.READ_WRITE);
        }
        else {
            folder.open(Folder.READ_ONLY);
        }
    }

    protected boolean hasResponse() throws IOException {
        return responseMessage != null;
    }

    protected Iterator getResponseHeaderNames() throws IOException {
        try {
            List headers = new ArrayList();
            Enumeration enumeration = responseMessage.getAllHeaders();
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

    protected Iterator getResponseHeaders(String name) throws IOException {
        try {
            String[] headers = responseMessage.getHeader(name);
            return Arrays.asList(headers).iterator();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);

        }
    }

    protected InputStream getResponseInputStream() throws IOException {
        try {
            return responseMessage.getDataHandler().getInputStream();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    public boolean hasError() throws IOException {
        return false;
    }

    public String getErrorMessage() throws IOException {
        return null;
    }

    public void close() throws IOException {
        MailTransportUtils.closeFolder(folder, deleteAfterReceive);
        MailTransportUtils.closeService(store);
    }

    private class ByteArrayDataSource implements DataSource {

        private byte[] data;

        private String contentType;

        public ByteArrayDataSource(String contentType, byte[] data) {
            this.data = data;
            this.contentType = contentType;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getContentType() {
            return contentType;
        }

        public String getName() {
            return "ByteArrayDataSource";
        }
    }

}
