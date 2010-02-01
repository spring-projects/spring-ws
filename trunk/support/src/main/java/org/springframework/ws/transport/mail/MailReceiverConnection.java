/*
 * Copyright 2005-2010 the original author or authors.
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.mail.support.MailTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for server-side Mail access. Exposes a {@link Message}
 * request and response message.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class MailReceiverConnection extends AbstractReceiverConnection {

    private final Message requestMessage;

    private final Session session;

    private Message responseMessage;

    private ByteArrayOutputStream responseBuffer;

    private String responseContentType;

    private URLName transportUri;

    private InternetAddress from;

    /** Constructs a new Mail connection with the given parameters. */
    protected MailReceiverConnection(Message requestMessage, Session session) {
        Assert.notNull(requestMessage, "'requestMessage' must not be null");
        Assert.notNull(session, "'session' must not be null");
        this.requestMessage = requestMessage;
        this.session = session;
    }

    /** Returns the request message for this connection. */
    public Message getRequestMessage() {
        return requestMessage;
    }

    /** Returns the response message, if any, for this connection. */
    public Message getResponseMessage() {
        return responseMessage;
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

    public URI getUri() throws URISyntaxException {
        try {
            Address[] recipients = requestMessage.getRecipients(Message.RecipientType.TO);
            if (!ObjectUtils.isEmpty(recipients) && recipients[0] instanceof InternetAddress) {
                return MailTransportUtils.toUri((InternetAddress) recipients[0], requestMessage.getSubject());
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

    public String getErrorMessage() throws IOException {
        return null;
    }

    public boolean hasError() throws IOException {
        return false;
    }

    /*
    * Receiving
    */

    @Override
    protected Iterator getRequestHeaderNames() throws IOException {
        try {
            List headers = new ArrayList();
            Enumeration enumeration = requestMessage.getAllHeaders();
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
    protected Iterator getRequestHeaders(String name) throws IOException {
        try {
            String[] headers = requestMessage.getHeader(name);
            return Arrays.asList(headers).iterator();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    @Override
    protected InputStream getRequestInputStream() throws IOException {
        try {
            return requestMessage.getInputStream();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    @Override
    protected void addResponseHeader(String name, String value) throws IOException {
        try {
            responseMessage.addHeader(name, value);
            if (TransportConstants.HEADER_CONTENT_TYPE.equals(name)) {
                responseContentType = value;
            }
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    @Override
    protected OutputStream getResponseOutputStream() throws IOException {
        return responseBuffer;
    }

    /*
     * Sending
     */

    @Override
    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        try {
            responseMessage = requestMessage.reply(false);
            responseMessage.setFrom(from);

            responseBuffer = new ByteArrayOutputStream();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    @Override
    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        Transport transport = null;
        try {
            responseMessage.setDataHandler(
                    new DataHandler(new ByteArrayDataSource(responseContentType, responseBuffer.toByteArray())));
            transport = session.getTransport(transportUri);
            transport.connect();
            responseMessage.saveChanges();
            transport.sendMessage(responseMessage, responseMessage.getAllRecipients());
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
        finally {
            MailTransportUtils.closeService(transport);
        }
    }

    private class ByteArrayDataSource implements DataSource {

        private byte[] data;

        private String contentType;

        public ByteArrayDataSource(String contentType, byte[] data) {
            this.data = data;
            this.contentType = contentType;
        }

        public String getContentType() {
            return contentType;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
