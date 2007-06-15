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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.springframework.util.Assert;
import org.springframework.ws.transport.AbstractReceiverConnection;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.mail.support.MailUtils;
import org.springframework.ws.transport.jms.JmsTransportException;
import org.springframework.ws.WebServiceMessage;

/** @author Arjen Poutsma */
public class MailReceiverConnection extends AbstractReceiverConnection {

    private final MimeMessage requestMessage;

    private final Session session;

    private MimeMessage responseMessage;

    private ByteArrayOutputStream responseBuffer;

    private String responseContentType;

    private URLName transportUri;

    public MailReceiverConnection(MimeMessage requestMessage, Session session) {
        Assert.notNull(requestMessage, "'requestMessage' must not be null");
        Assert.notNull(session, "'session' must not be null");
        this.requestMessage = requestMessage;
        this.session = session;
    }

    public String getErrorMessage() throws IOException {
        return null;
    }

    public boolean hasError() throws IOException {
        return false;
    }

    public void setTransportUri(URLName transportUri) {
        this.transportUri = transportUri;
    }

    public void close() throws IOException {
    }

    /*
     * Receiving
     */

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

    protected Iterator getRequestHeaders(String name) throws IOException {
        try {
            String[] headers = requestMessage.getHeader(name);
            return Arrays.asList(headers).iterator();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    protected InputStream getRequestInputStream() throws IOException {
        try {
            return requestMessage.getDataHandler().getInputStream();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

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

    protected OutputStream getResponseOutputStream() throws IOException {
        return responseBuffer;
    }

    /*
     * Sending
     */

    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        try {
            responseMessage = (MimeMessage) requestMessage.reply(false);

            responseBuffer = new ByteArrayOutputStream();
        }
        catch (MessagingException ex) {
            throw new MailTransportException(ex);
        }
    }

    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        Transport transport = null;
        try {
            requestMessage.setDataHandler(
                    new DataHandler(new ByteArrayDataSource(responseContentType, responseBuffer.toByteArray())));
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
