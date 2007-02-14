/*
 * Copyright 2006 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.soap.AttachmentPart;
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
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>SaajSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.SOAPMessage
 */
public class SaajSoapMessage extends AbstractSoapMessage {

    private static final String SOAP_ACTION_HEADER = "SOAPAction";

    private SOAPMessage saajMessage;

    private SoapEnvelope envelope;

    /**
     * Create a new <code>SaajSoapMessage</code> based on the given SAAJ <code>SOAPMessage</code>.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    public SaajSoapMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
    }

    /**
     * Return the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public SOAPMessage getSaajMessage() {
        return saajMessage;
    }

    /**
     * Sets the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public void setSaajMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
    }

    public SoapEnvelope getEnvelope() {
        if (envelope == null) {
            try {
                SOAPEnvelope saajEnvelope = getImplementation().getEnvelope(getSaajMessage());
                envelope = new SaajSoapEnvelope(saajEnvelope);
            }
            catch (SOAPException ex) {
                throw new SaajSoapEnvelopeException(ex);
            }
        }
        return envelope;
    }

    public String getSoapAction() {
        MimeHeaders mimeHeaders = getImplementation().getMimeHeaders(getSaajMessage());
        String[] values = mimeHeaders.getHeader(SOAP_ACTION_HEADER);
        return ObjectUtils.isEmpty(values) ? null : values[0];
    }

    public void setSoapAction(String soapAction) {
        MimeHeaders mimeHeaders = getImplementation().getMimeHeaders(getSaajMessage());
        mimeHeaders.setHeader(SOAP_ACTION_HEADER, soapAction);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            getImplementation().writeTo(getSaajMessage(), outputStream);
            outputStream.flush();
        }
        catch (SOAPException ex) {
            throw new SaajSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

    public Iterator getAttachments() throws AttachmentException {
        Iterator iterator = getImplementation().getAttachments(getSaajMessage());
        return new SaajAttachmentIterator(iterator);
    }

    public Attachment getAttachment(String contentId) {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Id", contentId);
        Iterator iterator = getImplementation().getAttachment(getSaajMessage(), mimeHeaders);
        if (!iterator.hasNext()) {
            return null;
        }
        else {
            AttachmentPart saajAttachment = (AttachmentPart) iterator.next();
            return new SaajAttachment(saajAttachment);
        }
    }

    public Attachment addAttachment(File file) throws AttachmentException {
        Assert.notNull(file, "File must not be null");
        DataSource dataSource = new FileDataSource(file);
        AttachmentPart attachmentPart = getImplementation().addAttachmentPart(getSaajMessage(), dataSource);
        return new SaajAttachment(attachmentPart);
    }

    public Attachment addAttachment(InputStreamSource inputStreamSource, String contentType) {
        Assert.notNull(inputStreamSource, "InputStreamSource must not be null");
        if (inputStreamSource instanceof Resource && ((Resource) inputStreamSource).isOpen()) {
            throw new IllegalArgumentException("Passed-in Resource contains an open stream: invalid argument. " +
                    "SAAJ requires an InputStreamSource that creates a fresh stream for every call.");
        }
        DataSource dataSource = new InputStreamSourceDataSource(inputStreamSource, contentType);
        AttachmentPart saajAttachment = getImplementation().addAttachmentPart(getSaajMessage(), dataSource);
        return new SaajAttachment(saajAttachment);
    }

    protected SaajImplementation getImplementation() {
        if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_13) {
            return Saaj13Implementation.getInstance();
        }
        else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_12) {
            return Saaj12Implementation.getInstance();
        }
        else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_11) {
            return Saaj11Implementation.getInstance();
        }
        else {
            throw new IllegalStateException("Could not find SAAJ on the classpath");
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
