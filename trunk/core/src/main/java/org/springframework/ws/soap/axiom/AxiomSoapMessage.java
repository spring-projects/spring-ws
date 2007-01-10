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
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.Assert;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * AXIOM-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>AxiomSoapMessageContext</code>.
 * <p/>
 * Note that Axiom does support reading SOAP with Attachments (SwA) messages, but does not support creating them
 * manually. Hence, the <code>addAttachment</code> methods throw an <code>UnsupportedOperationException</code>.
 *
 * @author Arjen Poutsma
 * @see SOAPMessage
 */
public class AxiomSoapMessage extends AbstractSoapMessage {

    private final SOAPMessage axiomMessage;

    private final SOAPFactory axiomFactory;

    private final Attachments attachments;

    private boolean payloadCaching;

    private AxiomSoapEnvelope envelope;

    private String soapAction;

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
        soapAction = "";
    }

    /**
     * Create a new <code>AxiomSoapMessage</code> based on the given AXIOM <code>SOAPMessage</code>.
     *
     * @param soapMessage    the AXIOM SOAPMessage
     * @param soapAction     the value of the SOAP Action header
     * @param payloadCaching whether the contents of the SOAP body should be cached or not
     */
    public AxiomSoapMessage(SOAPMessage soapMessage, String soapAction, boolean payloadCaching) {
        axiomMessage = soapMessage;
        axiomFactory = (SOAPFactory) soapMessage.getSOAPEnvelope().getOMFactory();
        attachments = null;
        this.soapAction = soapAction;
        this.payloadCaching = payloadCaching;
    }

    /**
     * Create a new <code>AxiomSoapMessage</code> based on the given AXIOM <code>SOAPMessage</code> and attachments.
     *
     * @param soapMessage    the AXIOM SOAPMessage
     * @param attachments    the attachments
     * @param soapAction     the value of the SOAP Action header
     * @param payloadCaching whether the contents of the SOAP body should be cached or not
     */
    public AxiomSoapMessage(SOAPMessage soapMessage,
                            Attachments attachments,
                            String soapAction,
                            boolean payloadCaching) {
        axiomMessage = soapMessage;
        axiomFactory = (SOAPFactory) soapMessage.getSOAPEnvelope().getOMFactory();
        this.attachments = attachments;
        this.soapAction = soapAction;
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

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public Attachment getAttachment(String contentId) {
        DataHandler dataHandler = attachments.getDataHandler(contentId);
        return dataHandler != null ? new AxiomAttachment(contentId, dataHandler) : null;
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
            String charsetEncoding = axiomMessage.getCharsetEncoding();

            OMOutputFormat format = new OMOutputFormat();
            format.setCharSetEncoding(charsetEncoding);
            format.setSOAP11(getVersion() == SoapVersion.SOAP_11);
            if (outputStream instanceof TransportOutputStream) {
                TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
                String contentType = format.getContentType();
                contentType += "; charset=\"" + charsetEncoding + "\"";
                transportOutputStream.addHeader("Content-Type", contentType);
            }
            axiomMessage.serializeAndConsume(outputStream, format);
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

        private final DataHandler dataHandler;

        private final String contentId;

        public AxiomAttachment(String contentId, DataHandler dataHandler) {
            Assert.notNull(contentId, "contentId must not be null");
            Assert.notNull(dataHandler, "dataHandler must not be null");
            this.contentId = contentId;
            this.dataHandler = dataHandler;
        }

        public String getId() {
            return contentId;
        }

        public void setId(String id) {
            throw new UnsupportedOperationException("Axiom does not support setting the Content-ID of attachments.");
        }

        public String getContentType() {
            return dataHandler.getContentType();
        }

        public InputStream getInputStream() throws IOException {
            return dataHandler.getInputStream();
        }

        public long getSize() {
            throw new UnsupportedOperationException("Axiom does not support getting the size of attachments.");
        }
    }

    private class AxiomAttachmentIterator implements Iterator {

        private final Iterator iterator;

        private AxiomAttachmentIterator() {
            iterator = attachments.getContentIDSet().iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            String contentId = (String) iterator.next();
            DataHandler dataHandler = attachments.getDataHandler(contentId);
            return new AxiomAttachment(contentId, dataHandler);
        }

        public void remove() {
            iterator.remove();
        }
    }

}
