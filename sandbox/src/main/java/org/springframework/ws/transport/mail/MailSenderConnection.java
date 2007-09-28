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
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.mail.support.MailUtils;

/**
 * @author Arjen Poutsma
 */
public class MailSenderConnection extends AbstractSenderConnection {

    private static final Log logger = LogFactory.getLog(MailSenderConnection.class);

    private final Session session;

    private final MailtoUri uri;

    private MimeMessage requestMessage;

    private Message responseMessage;

    private String requestContentType;

    private boolean deleteAfterReceive = false;

    private URLName storeUri;

    private URLName transportUri;

    private ByteArrayOutputStream requestBuffer;

    private InternetAddress from;

    protected MailSenderConnection(MailtoUri uri, Session session, InternetAddress from) {
        Assert.notNull(uri, "'uri' must not be null");
        Assert.notNull(session, "'session' must not be null");
        this.uri = uri;
        this.session = session;
        this.from = from;
    }

    public Message getRequestMessage() {
        return requestMessage;
    }

    public void setTransportUri(URLName transportUri) {
        this.transportUri = transportUri;
    }

    public void setStoreUri(URLName storeUri) {
        this.storeUri = storeUri;
    }

    /*
     * Sending
     */

    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        try {
            requestMessage = new MimeMessage(session);
            requestMessage.setFrom(from);
            requestMessage.setRecipient(Message.RecipientType.TO, uri.getTo());
            if (uri.hasCc()) {
                requestMessage.setRecipient(Message.RecipientType.CC, uri.getCc());
            }
            if (uri.hasSubject()) {
                requestMessage.setSubject(uri.getSubject());
            }
            requestMessage.setSentDate(new Date());

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
            MailUtils.closeService(transport);
        }
    }

    /*
     * Receiving
     */

    protected void onReceiveBeforeRead() throws IOException {
        Store store = null;
        Folder folder = null;
        try {
            String requestMessageId = null;
            if (requestMessage instanceof MimeMessage) {
                requestMessageId = ((MimeMessage) requestMessage).getMessageID();
            }
            if (StringUtils.hasLength(requestMessageId)) {
                try {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    logger.debug(e);
                }
                store = session.getStore(storeUri);
                store.connect();
                folder = store.getFolder(storeUri);
                if (folder == null || !folder.exists()) {
                    throw new MailTransportException("No default folder to receive from");
                }
                if (deleteAfterReceive) {
                    folder.open(Folder.READ_WRITE);
                }
                else {
                    folder.open(Folder.READ_ONLY);
                }
                SearchTerm searchTerm = new HeaderTerm(MailTransportConstants.HEADER_IN_REPLY_TO, requestMessageId);
                Message[] responses = folder.search(searchTerm);
                if (responses.length > 0) {
                    if (responses.length > 1) {
                        logger.warn("Received more than one response for request with ID [" + requestMessageId + "]");
                    }
                    responseMessage = (MimeMessage) responses[0];
                }
                if (deleteAfterReceive) {
                    responseMessage.setFlag(Flags.Flag.DELETED, true);
                }
            }
            else {
                logger.warn("Request message had no Message ID, could not find response");
            }
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
        finally {
            MailUtils.closeFolder(folder, deleteAfterReceive);
            MailUtils.closeService(store);
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
