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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.mime.AttachmentException;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.transport.TransportConstants;

/**
 * SAAJ-specific implementation of the {@link SoapMessage} interface. Created via the {@link SaajSoapMessageFactory},
 * wraps a {@link SOAPMessage}.
 *
 * @author Arjen Poutsma
 * @see SOAPMessage
 * @since 1.0.0
 */
public class SaajSoapMessage extends AbstractSoapMessage {

    private SOAPMessage saajMessage;

    private SoapEnvelope envelope;

    private static final String CONTENT_TYPE_XOP = "application/xop+xml";

    /**
     * Create a new <code>SaajSoapMessage</code> based on the given SAAJ <code>SOAPMessage</code>.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    public SaajSoapMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        MimeHeaders headers = getImplementation().getMimeHeaders(soapMessage);
        if (ObjectUtils.isEmpty(headers.getHeader(TransportConstants.HEADER_SOAP_ACTION))) {
            headers.addHeader(TransportConstants.HEADER_SOAP_ACTION, "\"\"");
        }
        saajMessage = soapMessage;
    }

    /** Return the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on. */
    public SOAPMessage getSaajMessage() {
        return saajMessage;
    }

    /** Sets the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on. */
    public void setSaajMessage(SOAPMessage soapMessage) {
        Assert.notNull(soapMessage, "soapMessage must not be null");
        saajMessage = soapMessage;
        envelope = null;
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
        String[] values = mimeHeaders.getHeader(TransportConstants.HEADER_SOAP_ACTION);
        return ObjectUtils.isEmpty(values) ? "" : values[0];
    }

    public void setSoapAction(String soapAction) {
        if (soapAction == null) {
            soapAction = "";
        }
        MimeHeaders mimeHeaders = getImplementation().getMimeHeaders(getSaajMessage());
        if (!soapAction.startsWith("\"")) {
            soapAction = "\"" + soapAction;
        }
        if (!soapAction.endsWith("\"")) {
            soapAction = soapAction + "\"";
        }
        mimeHeaders.setHeader(TransportConstants.HEADER_SOAP_ACTION, soapAction);
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

    public boolean isXopPackage() {
        if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
            SOAPPart saajPart = saajMessage.getSOAPPart();
            String[] contentTypes = saajPart.getMimeHeader(TransportConstants.HEADER_CONTENT_TYPE);
            for (int i = 0; i < contentTypes.length; i++) {
                if (contentTypes[i].indexOf(CONTENT_TYPE_XOP) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean convertToXopPackage() {
        if (SaajUtils.getSaajVersion() >= SaajUtils.SAAJ_13) {
            convertMessageToXop();
            convertPartToXop();
            return true;
        }
        else {
            return false;
        }
    }

    private void convertMessageToXop() {
        MimeHeaders mimeHeaders = saajMessage.getMimeHeaders();
        String[] oldContentTypes = mimeHeaders.getHeader(TransportConstants.HEADER_CONTENT_TYPE);
        String oldContentType =
                !ObjectUtils.isEmpty(oldContentTypes) ? oldContentTypes[0] : getVersion().getContentType();
        StringBuffer buffer = new StringBuffer(CONTENT_TYPE_XOP);
        buffer.append(";type=");
        buffer.append('"');
        buffer.append(oldContentType);
        buffer.append('"');
        mimeHeaders.setHeader(TransportConstants.HEADER_CONTENT_TYPE, buffer.toString());
    }

    private void convertPartToXop() {
        SOAPPart saajPart = saajMessage.getSOAPPart();
        String[] oldContentTypes = saajPart.getMimeHeader(TransportConstants.HEADER_CONTENT_TYPE);
        String oldContentType =
                !ObjectUtils.isEmpty(oldContentTypes) ? oldContentTypes[0] : getVersion().getContentType();
        StringBuffer buffer = new StringBuffer(CONTENT_TYPE_XOP);
        buffer.append(";type=");
        buffer.append('"');
        buffer.append(oldContentType);
        buffer.append('"');
        saajPart.setMimeHeader(TransportConstants.HEADER_CONTENT_TYPE, buffer.toString());
    }

    public Iterator getAttachments() throws AttachmentException {
        Iterator iterator = getImplementation().getAttachments(getSaajMessage());
        return new SaajAttachmentIterator(iterator);
    }

    public Attachment getAttachment(String contentId) {
        Assert.hasLength(contentId, "contentId must not be empty");
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.setHeader(TransportConstants.HEADER_CONTENT_ID, contentId);
        Iterator iterator = getImplementation().getAttachment(getSaajMessage(), mimeHeaders);
        if (!iterator.hasNext()) {
            return null;
        }
        AttachmentPart saajAttachment = (AttachmentPart) iterator.next();
        return new SaajAttachment(saajAttachment);
    }

    public Attachment addAttachment(String contentId, DataHandler dataHandler) {
        Assert.hasLength(contentId, "contentId must not be empty");
        Assert.notNull(dataHandler, "dataHandler must not be null");
        AttachmentPart saajAttachment = getImplementation().addAttachmentPart(getSaajMessage(), dataHandler);
        saajAttachment.setContentId(contentId);
        saajAttachment.setMimeHeader(TransportConstants.HEADER_CONTENT_TRANSFER_ENCODING, "binary");
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

    public String toString() {
        StringBuffer buffer = new StringBuffer("SaajSoapMessage");
        try {
            SOAPEnvelope envelope = getImplementation().getEnvelope(saajMessage);
            if (envelope != null) {
                SOAPBody body = getImplementation().getBody(envelope);
                if (body != null) {
                    SOAPElement bodyElement = getImplementation().getFirstBodyElement(body);
                    if (bodyElement != null) {
                        buffer.append(' ');
                        buffer.append(getImplementation().getName(bodyElement));
                    }
                }
            }
        }
        catch (SOAPException ex) {
            // ignore
        }
        return buffer.toString();
    }

    private static class SaajAttachmentIterator implements Iterator {

        private final Iterator saajIterator;

        private SaajAttachmentIterator(Iterator saajIterator) {
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
