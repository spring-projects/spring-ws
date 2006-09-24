/*
 * Copyright 2005 the original author or authors.
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
import java.util.Locale;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.AbstractSoapMessage;
import org.springframework.ws.soap.Attachment;
import org.springframework.ws.soap.AttachmentException;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.support.SaajUtils;

/**
 * SAAJ-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>SaajSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.SOAPMessage
 * @see SaajSoapMessageContext
 */
public class SaajSoapMessage extends AbstractSoapMessage {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String SOAP_ACTION_HEADER = "SOAPAction";

    private final SOAPMessage saajMessage;

    /**
     * Create a new <code>SaajSoapMessage</code> based on the given SAAJ <code>SOAPMessage</code>.
     *
     * @param soapMessage the SAAJ SOAPMessage
     */
    public SaajSoapMessage(SOAPMessage soapMessage) {
        this.saajMessage = soapMessage;
    }

    /**
     * Return the SAAJ <code>SOAPMessage</code> that this <code>SaajSoapMessage</code> is based on.
     */
    public final SOAPMessage getSaajMessage() {
        return this.saajMessage;
    }

    public SoapEnvelope getEnvelope() {
        try {
            return new SaajSoapEnvelope(saajMessage.getSOAPPart().getEnvelope());
        }
        catch (SOAPException ex) {
            throw new SaajSoapEnvelopeException(ex);
        }
    }

    public String getSoapAction() {
        String[] values = saajMessage.getMimeHeaders().getHeader(SOAP_ACTION_HEADER);
        return (ObjectUtils.isEmpty(values)) ? null : values[0];
    }

    public SoapVersion getVersion() {
        String[] contentTypes = saajMessage.getSOAPPart().getMimeHeader(CONTENT_TYPE_HEADER);
        if (ObjectUtils.isEmpty(contentTypes)) {
            throw new SaajSoapMessageException("Could not read '" + CONTENT_TYPE_HEADER + "' header from message");
        }
        else if (SoapVersion.SOAP_11.getContentType().equals(contentTypes[0])) {
            return SoapVersion.SOAP_11;
        }
        else if (SoapVersion.SOAP_12.getContentType().equals(contentTypes[0])) {
            return SoapVersion.SOAP_12;
        }
        else {
            throw new SaajSoapMessageException("Unknown content type [" + contentTypes[0] + "]");
        }
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            this.saajMessage.writeTo(outputStream);
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
     * SAAJ-Specific version of <code>org.springframework.ws.soap.SoapEnvelope</code>.
     */
    private static class SaajSoapEnvelope implements SoapEnvelope {

        private final SOAPEnvelope saajEnvelope;

        private SaajSoapEnvelope(SOAPEnvelope saajEnvelope) {
            this.saajEnvelope = saajEnvelope;
        }

        public QName getName() {
            return SaajUtils.toQName(saajEnvelope.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajEnvelope);
        }

        public SoapHeader getHeader() {
            try {
                return (saajEnvelope.getHeader() != null) ? new SaajSoapHeader(saajEnvelope.getHeader()) : null;
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }

        public SoapBody getBody() {
            try {
                return new SaajSoapBody(saajEnvelope.getBody());
            }
            catch (SOAPException ex) {
                throw new SaajSoapBodyException(ex);
            }
        }
    }

    /**
     * SAAJ-Specific version of <code>org.springframework.ws.soap.SoapHeader</code>.
     */
    private static class SaajSoapHeader implements SoapHeader {

        private final SOAPHeader saajHeader;

        private SaajSoapHeader(SOAPHeader saajHeader) {
            this.saajHeader = saajHeader;
        }

        public QName getName() {
            return SaajUtils.toQName(saajHeader.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajHeader);
        }

        public SoapHeaderElement addHeaderElement(QName name) {
            try {
                Name saajName = SaajUtils.toName(name, getEnvelope());
                SOAPHeaderElement saajHeaderElement = saajHeader.addHeaderElement(saajName);
                return new SaajSoapHeaderElement(saajHeaderElement);
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }

        public Iterator examineMustUnderstandHeaderElements(String role) {
            return new SaajSoapHeaderElementIterator(saajHeader.examineMustUnderstandHeaderElements(role));
        }

        public Iterator examineAllHeaderElements() {
            return new SaajSoapHeaderElementIterator(saajHeader.examineAllHeaderElements());
        }

        private SOAPEnvelope getEnvelope() {
            return (SOAPEnvelope) saajHeader.getParentElement();
        }

        private static class SaajSoapHeaderElementIterator implements Iterator {

            private final Iterator saajIterator;

            private SaajSoapHeaderElementIterator(Iterator saajIterator) {
                this.saajIterator = saajIterator;
            }

            public boolean hasNext() {
                return saajIterator.hasNext();
            }

            public Object next() {
                SOAPHeaderElement saajHeaderElement = (SOAPHeaderElement) saajIterator.next();
                return new SaajSoapHeaderElement(saajHeaderElement);
            }

            public void remove() {
                saajIterator.remove();
            }
        }
    }

    /**
     * SAAJ-Specific version of <code>org.springframework.ws.soap.SoapHeaderElement</code>.
     */
    private static class SaajSoapHeaderElement implements SoapHeaderElement {

        private final SOAPHeaderElement saajHeaderElement;

        private SaajSoapHeaderElement(SOAPHeaderElement saajHeaderElement) {
            this.saajHeaderElement = saajHeaderElement;
        }

        public QName getName() {
            return SaajUtils.toQName(saajHeaderElement.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajHeaderElement);
        }

        public String getRole() {
            return saajHeaderElement.getActor();
        }

        public void setRole(String role) {
            saajHeaderElement.setActor(role);
        }

        public boolean getMustUnderstand() {
            return saajHeaderElement.getMustUnderstand();
        }

        public void setMustUnderstand(boolean mustUnderstand) {
            saajHeaderElement.setMustUnderstand(mustUnderstand);
        }

        public Result getResult() {
            return new DOMResult(saajHeaderElement);
        }

        public void addAttribute(QName name, String value) throws SoapHeaderException {
            try {
                Name saajName = SaajUtils.toName(name, getEnvelope());
                saajHeaderElement.addAttribute(saajName, value);
            }
            catch (SOAPException ex) {
                throw new SaajSoapHeaderException(ex);
            }
        }

        private SOAPEnvelope getEnvelope() {
            return (SOAPEnvelope) saajHeaderElement.getParentElement().getParentElement();
        }

    }

    /**
     * SAAJ-specific implementation of <code>org.springframework.ws.soap.SoapBody</code>.
     */
    private static class SaajSoapBody implements SoapBody {

        private final SOAPBody saajBody;

        private SaajSoapBody(SOAPBody saajBody) {
            this.saajBody = saajBody;
        }

        public Source getPayloadSource() {
            SOAPBodyElement payloadElement = getPayloadElement();
            return (payloadElement != null) ? new DOMSource(payloadElement) : null;
        }

        public Result getPayloadResult() {
            return new DOMResult(saajBody);
        }

        public SoapFault addFault(QName faultCode, String faultString) {
            Assert.hasLength("faultString cannot be empty", faultString);
            if (!StringUtils.hasLength(faultCode.getNamespaceURI()) || (!StringUtils.hasLength(faultCode.getPrefix())))
            {
                throw new IllegalArgumentException("A fully qualified fault code (namespace, prefix, and local part) " +
                        "must be specific for a custom fault code");
            }
            try {
                Name name;
                if (faultCode.getNamespaceURI().equals(SOAPConstants.URI_NS_SOAP_ENVELOPE)) {
                    name = getEnvelope()
                            .createName(faultCode.getLocalPart(), null, SOAPConstants.URI_NS_SOAP_ENVELOPE);
                }
                else {
                    name = SaajUtils.toName(faultCode, getEnvelope());
                }
                for (Iterator iterator = saajBody.getChildElements(); iterator.hasNext();) {
                    SOAPBodyElement bodyElement = (SOAPBodyElement) iterator.next();
                    bodyElement.detachNode();
                    bodyElement.recycleNode();
                }
                SOAPFault saajFault = saajBody.addFault(name, faultString);
                return new SaajSoapFault(saajFault);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public boolean hasFault() {
            return saajBody.hasFault();
        }

        public SoapFault getFault() {
            return new SaajSoapFault(saajBody.getFault());
        }

        public QName getName() {
            return SaajUtils.toQName(saajBody.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajBody);
        }

        private SOAPEnvelope getEnvelope() {
            return (SOAPEnvelope) saajBody.getParentElement();
        }

        /**
         * Retrieves the payload of the wrapped SAAJ message as a single DOM element. The payload of a message is the
         * contents of the SOAP body.
         *
         * @return the message payload, or <code>null</code> if none is set.
         */
        private SOAPBodyElement getPayloadElement() {
            for (Iterator iterator = saajBody.getChildElements(); iterator.hasNext();) {
                Object child = iterator.next();
                if (child instanceof SOAPBodyElement) {
                    return (SOAPBodyElement) child;
                }
            }
            return null;
        }
    }

    /**
     * SAAJ-specific implementation of <code>org.springframework.ws.soap.SoapFault</code>.
     */
    private static class SaajSoapFault implements SoapFault {

        private static final String MUST_UNDERSTAND = "MustUnderstand";

        private static final String SERVER = "Server";

        private static final String CLIENT = "Client";

        private final SOAPFault saajFault;

        private SaajSoapFault(SOAPFault saajFault) {
            this.saajFault = saajFault;
        }

        public QName getName() {
            return SaajUtils.toQName(saajFault.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajFault);
        }

        public QName getFaultCode() {
            return SaajUtils.toQName(saajFault.getFaultCodeAsName());
        }

        public String getFaultString() {
            return saajFault.getFaultString();
        }

        public void setFaultString(String faultString, Locale locale) {
            try {
                saajFault.setFaultString(faultString, locale);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public Locale getFaultStringLocale() {
            return saajFault.getFaultStringLocale();
        }

        public String getFaultRole() {
            return saajFault.getFaultActor();
        }

        public void setFaultRole(String role) {
            try {
                saajFault.setFaultActor(role);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public SoapFaultDetail getFaultDetail() {
            Detail saajDetail = saajFault.getDetail();
            return (saajDetail != null) ? new SaajSoapFaultDetail(saajDetail) : null;
        }

        public SoapFaultDetail addFaultDetail() {
            try {
                Detail saajDetail = saajFault.addDetail();
                return (saajDetail != null) ? new SaajSoapFaultDetail(saajDetail) : null;
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public boolean isMustUnderstandFault() {
            return (MUST_UNDERSTAND.equals(saajFault.getFaultCodeAsName().getLocalName()) &&
                    SOAPConstants.URI_NS_SOAP_ENVELOPE.equals(saajFault.getFaultCodeAsName().getURI()));
        }

        public boolean isSenderFault() {
            return (CLIENT.equals(saajFault.getFaultCodeAsName().getLocalName()) &&
                    SOAPConstants.URI_NS_SOAP_ENVELOPE.equals(saajFault.getFaultCodeAsName().getURI()));
        }

        public boolean isReceiverFault() {
            return (SERVER.equals(saajFault.getFaultCodeAsName().getLocalName()) &&
                    SOAPConstants.URI_NS_SOAP_ENVELOPE.equals(saajFault.getFaultCodeAsName().getURI()));
        }
    }

    /**
     * SAAJ-specific implementation of <code>org.springframework.ws.soap.SoapFaultDetail</code>
     */
    private static class SaajSoapFaultDetail implements SoapFaultDetail {

        private Detail saajDetail;

        private SaajSoapFaultDetail(Detail saajDetail) {
            this.saajDetail = saajDetail;
        }

        public QName getName() {
            return SaajUtils.toQName(saajDetail.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajDetail);
        }

        public SoapFaultDetailElement addFaultDetailElement(QName name) {
            try {
                Name detailEntryName = SaajUtils.toName(name, getEnvelope());
                DetailEntry saajDetailEntry = saajDetail.addDetailEntry(detailEntryName);
                return new SaajSoapFaultDetailElement(saajDetailEntry);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public Iterator getDetailEntries() {
            return new SaajSoapFaultDetailIterator(saajDetail.getDetailEntries());
        }

        private SOAPEnvelope getEnvelope() {
            return (SOAPEnvelope) saajDetail.getParentElement().getParentElement().getParentElement();
        }

    }

    private static class SaajSoapFaultDetailElement implements SoapFaultDetailElement {

        private DetailEntry saajDetailEntry;

        private SaajSoapFaultDetailElement(DetailEntry saajDetailEntry) {
            this.saajDetailEntry = saajDetailEntry;
        }

        public Result getResult() {
            return new DOMResult(saajDetailEntry);
        }

        public void addText(String text) {
            try {
                this.saajDetailEntry.addTextNode(text);
            }
            catch (SOAPException ex) {
                throw new SaajSoapFaultException(ex);
            }
        }

        public QName getName() {
            return SaajUtils.toQName(saajDetailEntry.getElementName());
        }

        public Source getSource() {
            return new DOMSource(saajDetailEntry);
        }
    }

    private static class SaajSoapFaultDetailIterator implements Iterator {

        private final Iterator saajIterator;

        public SaajSoapFaultDetailIterator(Iterator saajIterator) {
            this.saajIterator = saajIterator;
        }

        public boolean hasNext() {
            return saajIterator.hasNext();
        }

        public Object next() {
            DetailEntry saajDetailEntry = (DetailEntry) saajIterator.next();
            return new SaajSoapFaultDetailElement(saajDetailEntry);
        }

        public void remove() {
            saajIterator.remove();
        }

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
                return (result != -1) ? result : 0;
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
