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

package org.springframework.ws.soap.axiom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import javax.mail.MessagingException;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.Part;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapEnvelope;

/**
 * AXIOM-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>AxiomSoapMessageContext</code>.
 * <p/>
 * Note that Axiom does support reading SOAP with Attachments (SwA) messages, but does not support creating them
 * manually. Hence, the <code>addAttachment</code> methods throw an <code>UnsupportedOperationException</code>.
 *
 * @author Arjen Poutsma
 * @see SOAPMessage
 * @see AxiomSoapMessageContext
 */
public class AxiomSoapMessage extends AbstractSoapMessage {

    private final SOAPMessage axiomMessage;

    private final SOAPFactory axiomFactory;

    private final Attachments attachments;

    private boolean payloadCaching;

    private AxiomSoapEnvelope envelope;

    /**
     * Create a new, empty <code>AxiomSoapMessage</code>.
     *
     * @param soapFactory the AXIOM SOAPFactory
     */
    public AxiomSoapMessage(SOAPFactory soapFactory) {
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        axiomFactory = soapFactory;
        axiomMessage = axiomFactory.createSOAPMessage(soapEnvelope, soapEnvelope.getBuilder());
        attachments = null;
        payloadCaching = true;
    }

    /**
     * Create a new <code>AxiomSoapMessage</code> based on the given AXIOM <code>SOAPMessage</code>.
     *
     * @param soapMessage    the AXIOM SOAPMessage
     * @param attachments    the attachments
     * @param payloadCaching whether the contents of the SOAP body should be cached or not
     */
    public AxiomSoapMessage(SOAPMessage soapMessage, Attachments attachments, boolean payloadCaching) {
        axiomMessage = soapMessage;
        axiomFactory = (SOAPFactory) soapMessage.getSOAPEnvelope().getOMFactory();
        this.attachments = attachments;
        this.payloadCaching = payloadCaching;
    }

    /**
     * Return the AXIOM <code>SOAPMessage</code> that this <code>AxiomSoapMessage</code> is based on.
     */
    public final SOAPMessage getAxiomMessage() {
        return axiomMessage;
    }

    public SoapEnvelope getEnvelope() {
        if (envelope == null) {
            try {
                envelope = new AxiomSoapEnvelope(axiomMessage.getSOAPEnvelope(), axiomFactory, payloadCaching);
            }
            catch (SOAPProcessingException ex) {
                throw new AxiomSoapEnvelopeException(ex);
            }
        }
        return envelope;
    }

    public Attachment getAttachment(String contentId) {
        Part part = attachments.getPart(contentId);
        return part != null ? new AxiomAttachment(part) : null;
    }

    public Iterator getAttachments() {
        return new AxiomAttachmentIterator();
    }

    /**
     * Axiom does not support adding attachments manually.
     *
     * @throws UnsupportedOperationException always
     */
    public Attachment addAttachment(File file) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Axiom does not support adding SwA attachments.");
    }

    /**
     * Axiom does not support adding attachments manually.
     *
     * @throws UnsupportedOperationException always
     */
    public Attachment addAttachment(InputStreamSource inputStreamSource, String contentType)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Axiom does not support adding SwA attachments.");
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            axiomMessage.serialize(outputStream);
        }
        catch (XMLStreamException ex) {
            throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
        catch (OMException ex) {
            throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

    /**
     * Axiom-specific implementation of <code>org.springframework.ws.soap.Attachment</code>
     */
    private static class AxiomAttachment implements Attachment {

        private final Part part;

        private AxiomAttachment(Part part) {
            this.part = part;
        }

        public String getId() {
            try {
                return part.getContentID();
            }
            catch (MessagingException ex) {
                throw new AxiomAttachmentException(ex);
            }
        }

        public void setId(String id) {
            throw new UnsupportedOperationException("Axiom does not support setting the Content-ID of attachments.");
        }

        public String getContentType() {
            try {
                return part.getContentType();
            }
            catch (MessagingException ex) {
                throw new AxiomAttachmentException(ex);
            }
        }

        public InputStream getInputStream() throws IOException {
            try {
                return part.getInputStream();
            }
            catch (MessagingException ex) {
                throw new AxiomAttachmentException(ex);
            }
        }

        public long getSize() {
            try {
                return part.getSize();
            }
            catch (MessagingException ex) {
                throw new AxiomAttachmentException(ex);
            }
        }
    }

    private class AxiomAttachmentIterator implements Iterator {

        private final Iterator iterator;

        private AxiomAttachmentIterator() {
            iterator = Arrays.asList(attachments.getAllContentIDs()).iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            String contentId = (String) iterator.next();
            Part part = attachments.getPart(contentId);
            return part != null ? new AxiomAttachment(part) : null;
        }

        public void remove() {
            iterator.remove();
        }
    }

}
