/*
 * Copyright 2005, 2006 the original author or authors.
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

package org.springframework.ws.soap.saaj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.AttachmentException;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * SAAJ-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>SaajSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.SOAPMessage
 */
public abstract class SaajSoapMessage extends AbstractSoapMessage {

    private SOAPMessage saajMessage;

    private SoapEnvelope envelope;

    /**
     * Create a new <code>SaajSoapMessage</code> based on the given SAAJ <code>SOAPMessage</code>.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    protected SaajSoapMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
    }

    /**
     * Return the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public final SOAPMessage getSaajMessage() {
        return saajMessage;
    }

    /**
     * Sets the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public final void setSaajMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
    }

    public final SoapEnvelope getEnvelope() {
        if (envelope == null) {
            try {
                SOAPEnvelope saajEnvelope = saajMessage.getSOAPPart().getEnvelope();
                envelope = createSaajSoapEnvelope(saajEnvelope);
            }
            catch (SOAPException ex) {
                throw new SaajSoapEnvelopeException(ex);
            }
        }
        return envelope;
    }

    protected abstract SoapEnvelope createSaajSoapEnvelope(SOAPEnvelope saajEnvelope);

    public final void writeTo(OutputStream outputStream) throws IOException {
        try {
            if (saajMessage.saveRequired()) {
                saajMessage.saveChanges();
            }
            if (outputStream instanceof TransportOutputStream) {
                TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
                // some SAAJ implementations (Axis 1) do not have a Content-Type header by default
                MimeHeaders headers = saajMessage.getMimeHeaders();
                if (ObjectUtils.isEmpty(headers.getHeader("Content-Type"))) {
                    headers.addHeader("Content-Type", getVersion().getContentType());
                    if (saajMessage.saveRequired()) {
                        saajMessage.saveChanges();
                    }
                }
                for (Iterator iterator = headers.getAllHeaders(); iterator.hasNext();) {
                    MimeHeader mimeHeader = (MimeHeader) iterator.next();
                    transportOutputStream.addHeader(mimeHeader.getName(), mimeHeader.getValue());
                }
            }
            saajMessage.writeTo(outputStream);
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

    public Attachment getAttachment(String contentId) {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Id", contentId);
        Iterator iterator = saajMessage.getAttachments(mimeHeaders);
        if (!iterator.hasNext()) {
            return null;
        }
        else {
            AttachmentPart saajAttachment = (AttachmentPart) iterator.next();
            return new SaajAttachment(saajAttachment);
        }
    }

    public Iterator getAttachments() {
        Iterator saajAttachmentIterator = saajMessage.getAttachments();
        return new SaajAttachmentIterator(saajAttachmentIterator);
    }

    public Attachment addAttachment(File file) throws AttachmentException {
        Assert.notNull(file, "File must not be null");
        FileDataSource dataSource = new FileDataSource(file);
        AttachmentPart saajAttachment = saajMessage.createAttachmentPart(new DataHandler(dataSource));
        saajMessage.addAttachmentPart(saajAttachment);
        return new SaajAttachment(saajAttachment);
    }

    public Attachment addAttachment(InputStreamSource inputStreamSource, String contentType) {
        Assert.notNull(inputStreamSource, "InputStreamSource must not be null");
        if (inputStreamSource instanceof Resource && ((Resource) inputStreamSource).isOpen()) {
            throw new IllegalArgumentException("Passed-in Resource contains an open stream: invalid argument. " +
                    "SAAJ requires an InputStreamSource that creates a fresh stream for every call.");
        }
        DataSource dataSource = createDataSource(inputStreamSource, contentType);
        AttachmentPart saajAttachment = saajMessage.createAttachmentPart(new DataHandler(dataSource));
        saajMessage.addAttachmentPart(saajAttachment);
        return new SaajAttachment(saajAttachment);
    }

    /**
     * Create an Activation Framework DataSource for the given InputStreamSource.
     *
     * @param inputStreamSource the InputStreamSource (typically a Spring Resource)
     * @param contentType       the content type
     * @return the Activation Framework DataSource
     */
    private DataSource createDataSource(final InputStreamSource inputStreamSource, final String contentType) {
        return new DataSource() {
            public InputStream getInputStream() throws IOException {
                return inputStreamSource.getInputStream();
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
            }

            public String getContentType() {
                return contentType;
            }

            public String getName() {
                throw new UnsupportedOperationException("DataSource name not available");
            }
        };
    }

    /**
     * SAAJ-specific implementation of <code>org.springframework.ws.soap.Attachment</code>
     */
    private static class SaajAttachment implements Attachment {

        private final AttachmentPart saajAttachment;

        public SaajAttachment(AttachmentPart saajAttachment) {
            this.saajAttachment = saajAttachment;
        }

        public String getId() {
            return saajAttachment.getContentId();
        }

        public void setId(String id) {
            saajAttachment.setContentId(id);
        }

        public String getContentType() {
            return saajAttachment.getContentType();
        }

        public InputStream getInputStream() throws IOException {
            try {
                return saajAttachment.getDataHandler().getInputStream();
            }
            catch (SOAPException e) {
                return new ByteArrayInputStream(new byte[0]);
            }
        }

        public long getSize() {
            try {
                int result = saajAttachment.getSize();
                // SAAJ returns -1 when the size cannot be determined
                return result != -1 ? result : 0;
            }
            catch (SOAPException ex) {
                throw new SaajAttachmentException(ex);
            }
        }

    }

    private static class SaajAttachmentIterator implements Iterator {

        private final Iterator saajIterator;

        public SaajAttachmentIterator(Iterator saajIterator) {
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            AttachmentPart saajAttachment = (AttachmentPart) saajIterator.next();
            return new SaajAttachment(saajAttachment);
        }

        public void remove() {
            saajIterator.remove();
        }
    }


}
