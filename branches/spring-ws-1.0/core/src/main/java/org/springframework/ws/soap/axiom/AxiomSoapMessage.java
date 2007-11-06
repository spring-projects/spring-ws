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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.SOAPProcessingException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * AXIOM-specific implementation of the {@link SoapMessage} interface. Created via the {@link AxiomSoapMessageFactory},
 * wraps a {@link SOAPMessage}.
 *
 * @author Arjen Poutsma
 * @see SOAPMessage
 * @since 1.0.0
 */
public class AxiomSoapMessage extends AbstractSoapMessage {

    private final SOAPMessage axiomMessage;

    private final SOAPFactory axiomFactory;

    private final Attachments attachments;

    private final boolean payloadCaching;

    private AxiomSoapEnvelope envelope;

    private String soapAction;

    /**
     * Create a new, empty <code>AxiomSoapMessage</code>.
     *
     * @param soapFactory the AXIOM SOAPFactory
     */
    public AxiomSoapMessage(SOAPFactory soapFactory) {
        this(soapFactory, true);
    }

    /**
     * Create a new, empty <code>AxiomSoapMessage</code>.
     *
     * @param soapFactory the AXIOM SOAPFactory
     */
    public AxiomSoapMessage(SOAPFactory soapFactory, boolean payloadCaching) {
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        axiomFactory = soapFactory;
        axiomMessage = axiomFactory.createSOAPMessage(soapEnvelope, soapEnvelope.getBuilder());
        attachments = new Attachments();
        this.payloadCaching = payloadCaching;
        soapAction = "\"\"";
    }

    /**
     * Create a new <code>AxiomSoapMessage</code> based on the given AXIOM <code>SOAPMessage</code>.
     *
     * @param soapMessage    the AXIOM SOAPMessage
     * @param soapAction     the value of the SOAP Action header
     * @param payloadCaching whether the contents of the SOAP body should be cached or not
     */
    public AxiomSoapMessage(SOAPMessage soapMessage, String soapAction, boolean payloadCaching) {
        this(soapMessage, new Attachments(), soapAction, payloadCaching);
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
        Assert.notNull(soapMessage, "'soapMessage' must not be null");
        Assert.notNull(attachments, "'attachments' must not be null");
        axiomMessage = soapMessage;
        axiomFactory = (SOAPFactory) soapMessage.getSOAPEnvelope().getOMFactory();
        this.attachments = attachments;
        if (!StringUtils.hasLength(soapAction)) {
            soapAction = "\"\"";
        }
        this.soapAction = soapAction;
        this.payloadCaching = payloadCaching;
    }

    /** Return the AXIOM <code>SOAPMessage</code> that this <code>AxiomSoapMessage</code> is based on. */
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
        if (soapAction == null) {
            soapAction = "";
        }
        if (!soapAction.startsWith("\"")) {
            soapAction = "\"" + soapAction;
        }
        if (!soapAction.endsWith("\"")) {
            soapAction = soapAction + "\"";
        }
        this.soapAction = soapAction;
    }

    public boolean isXopPackage() {
        try {
            return MTOMConstants.MTOM_TYPE.equals(attachments.getAttachmentSpecType());
        }
        catch (NullPointerException ex) {
            // gotta love Axis2
            return false;
        }
    }

    public boolean convertToXopPackage() {
        return false;
    }

    public Attachment getAttachment(String contentId) {
        Assert.hasLength(contentId, "contentId must not be empty");
        if (contentId.startsWith("<") && contentId.endsWith(">")) {
            contentId = contentId.substring(1, contentId.length() - 1);
        }
        DataHandler dataHandler = attachments.getDataHandler(contentId);
        return dataHandler != null ? new AxiomAttachment(contentId, dataHandler) : null;
    }

    public Iterator getAttachments() {
        return new AxiomAttachmentIterator();
    }

    public Attachment addAttachment(String contentId, DataHandler dataHandler) {
        Assert.hasLength(contentId, "contentId must not be empty");
        Assert.notNull(dataHandler, "dataHandler must not be null");
        attachments.addDataHandler(contentId, dataHandler);
        return new AxiomAttachment(contentId, dataHandler);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            String charsetEncoding = axiomMessage.getCharsetEncoding();

            OMOutputFormat format = new OMOutputFormat();
            format.setCharSetEncoding(charsetEncoding);
            format.setSOAP11(getVersion() == SoapVersion.SOAP_11);
            if (!attachments.getContentIDSet().isEmpty()) {
                format.setDoingSWA(true);
            }
            if (outputStream instanceof TransportOutputStream) {
                TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
                String contentType = format.getContentType();
                contentType += "; charset=\"" + charsetEncoding + "\"";
                transportOutputStream.addHeader(TransportConstants.HEADER_CONTENT_TYPE, contentType);
                transportOutputStream.addHeader(TransportConstants.HEADER_SOAP_ACTION, soapAction);
            }
            if (payloadCaching) {
                axiomMessage.serialize(outputStream, format);
            }
            else {
                axiomMessage.serializeAndConsume(outputStream, format);
            }
            outputStream.flush();
        }
        catch (XMLStreamException ex) {
            throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
        catch (OMException ex) {
            throw new AxiomSoapMessageException("Could not write message to OutputStream: " + ex.getMessage(), ex);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("AxiomSoapMessage");
        if (payloadCaching) {
            try {
                SOAPEnvelope envelope = axiomMessage.getSOAPEnvelope();
                if (envelope != null) {
                    SOAPBody body = envelope.getBody();
                    if (body != null) {
                        OMElement bodyElement = body.getFirstElement();
                        if (bodyElement != null) {
                            buffer.append(' ');
                            buffer.append(bodyElement.getQName());
                        }
                    }
                }
            }
            catch (OMException ex) {
                // ignore
            }
        }
        return buffer.toString();
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
