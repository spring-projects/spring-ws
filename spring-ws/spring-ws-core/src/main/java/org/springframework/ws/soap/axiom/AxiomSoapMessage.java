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
import java.util.Locale;

import javax.mail.MessagingException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.attachments.Part;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.SAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultRole;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.SOAPMessage;
import org.xml.sax.SAXException;

import org.springframework.core.io.InputStreamSource;
import org.springframework.util.Assert;
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
import org.springframework.xml.transform.StaxSource;

/**
 * AXIOM-specific implementation of the <code>SoapMessage</code> interface. Accessed via the
 * <code>AxiomSoapMessageContext</code>.
 *
 * @author Arjen Poutsma
 * @see SOAPMessage
 * @see AxiomSoapMessageContext
 */
public class AxiomSoapMessage extends AbstractSoapMessage {

    private final SOAPMessage axiomMessage;

    private final SOAPFactory axiomFactory;

    private final String soapAction;

    private final Attachments attachments;

    /**
     * Create a new, empty <code>AxiomSoapMessage</code>.
     *
     * @param soapFactory the AXIOM SOAPFactory
     */
    public AxiomSoapMessage(SOAPFactory soapFactory) {
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        this.axiomFactory = soapFactory;
        this.axiomMessage = axiomFactory.createSOAPMessage(soapEnvelope, soapEnvelope.getBuilder());
        this.soapAction = null;
        this.attachments = null;
    }

    /**
     * Create a new <code>AxiomSoapMessage</code> based on the given AXIOM <code>SOAPMessage</code>.
     *
     * @param soapMessage the AXIOM SOAPMessage
     * @param soapFactory the AXIOM SOAPFactory
     * @param soapAction  the value of SOAP Action header
     */
    public AxiomSoapMessage(SOAPMessage soapMessage, SOAPFactory soapFactory, String soapAction) {
        this.axiomMessage = soapMessage;
        this.axiomFactory = soapFactory;
        this.soapAction = soapAction;
        this.attachments = null;
    }

    public AxiomSoapMessage(SOAPMessage axiomMessage,
                            SOAPFactory axiomFactory,
                            String soapAction,
                            Attachments attachments) {
        this.axiomMessage = axiomMessage;
        this.axiomFactory = axiomFactory;
        this.soapAction = soapAction;
        this.attachments = attachments;
    }

    /**
     * Return the AXIOM <code>SOAPMessage</code> that this <code>AxiomSoapMessage</code> is based on.
     */
    public final SOAPMessage getAxiomMessage() {
        return this.axiomMessage;
    }

    public SoapEnvelope getEnvelope() {
        try {
            return new AxiomSoapEnvelope(axiomMessage.getSOAPEnvelope());
        }
        catch (OMException ex) {
            throw new AxiomSoapEnvelopeException(ex);
        }
    }

    public String getSoapAction() {
        return soapAction;
    }

    public SoapVersion getVersion() {
        String envelopeNamespace = axiomMessage.getSOAPEnvelope().getNamespace().getName();
        if (SoapVersion.SOAP_11.getEnvelopeNamespaceUri().equals(envelopeNamespace)) {
            return SoapVersion.SOAP_11;
        }
        else if (SoapVersion.SOAP_12.getEnvelopeNamespaceUri().equals(envelopeNamespace)) {
            return SoapVersion.SOAP_12;
        }
        else {
            throw new IllegalStateException(
                    "Unknown Envelope namespace uri '" + envelopeNamespace + "'. " + "Cannot deduce SoapVersion.");
        }
    }

    public Attachment getAttachment(String contentId) {
        Part part = attachments.getPart(contentId);
        return part != null ? new AxiomAttachment(part) : null;
    }

    public Iterator getAttachments() {
        return new AxiomAttachmentIterator();
    }

    public Attachment addAttachment(File file) throws AttachmentException {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
    }

    public Attachment addAttachment(InputStreamSource inputStreamSource, String contentType) {
        //TODO implement
        throw new UnsupportedOperationException("Not implemented");
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
     * Axiom-Specific version of <code>org.springframework.ws.soap.SoapEnvelope</code>.
     */
    private class AxiomSoapEnvelope implements SoapEnvelope {

        private final SOAPEnvelope axiomEnvelope;

        public AxiomSoapEnvelope(SOAPEnvelope axiomEnvelope) {
            this.axiomEnvelope = axiomEnvelope;
        }

        public QName getName() {
            return axiomEnvelope.getQName();
        }

        public Source getSource() {
            try {
                return new StaxSource(axiomEnvelope.getXMLStreamReader());
            }
            catch (OMException ex) {
                throw new AxiomSoapEnvelopeException(ex);
            }
        }

        public SoapHeader getHeader() {
            try {
                SOAPHeader axiomHeader = axiomEnvelope.getHeader();
                return (axiomHeader != null) ? new AxiomSoapHeader(axiomHeader) : null;
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }
        }

        public SoapBody getBody() {
            try {
                SOAPBody axiomBody = axiomEnvelope.getBody();
                return (axiomBody != null) ? new AxiomSoapBody(axiomBody) : null;
            }
            catch (OMException ex) {
                throw new AxiomSoapBodyException(ex);
            }
        }
    }

    /**
     * Axiom-specific version of <code>org.springframework.ws.soap.SoapHeader</code>.
     */
    private class AxiomSoapHeader implements SoapHeader {

        private final SOAPHeader axiomHeader;

        private AxiomSoapHeader(SOAPHeader axiomHeader) {
            this.axiomHeader = axiomHeader;
        }

        public QName getName() {
            return axiomHeader.getQName();
        }

        public Source getSource() {
            return new StaxSource(axiomHeader.getXMLStreamReader());
        }

        public SoapHeaderElement addHeaderElement(QName name) {
            try {
                OMNamespace namespace = axiomFactory.createOMNamespace(name.getNamespaceURI(), name.getPrefix());
                SOAPHeaderBlock axiomHeaderBlock = axiomHeader.addHeaderBlock(name.getLocalPart(), namespace);
                return new AxiomSoapHeaderElement(axiomHeaderBlock);
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }
        }

        public Iterator examineMustUnderstandHeaderElements(String role) {
            try {
                return new AxiomSoapHeaderElementIterator(axiomHeader.examineMustUnderstandHeaderBlocks(role));
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }
        }

        public Iterator examineAllHeaderElements() {
            try {
                return new AxiomSoapHeaderElementIterator(axiomHeader.examineAllHeaderBlocks());
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }

        }

        private class AxiomSoapHeaderElementIterator implements Iterator {

            private final Iterator axiomIterator;

            private AxiomSoapHeaderElementIterator(Iterator axiomIterator) {
                this.axiomIterator = axiomIterator;
            }

            public boolean hasNext() {
                return axiomIterator.hasNext();
            }

            public Object next() {
                try {
                    SOAPHeaderBlock axiomHeaderBlock = (SOAPHeaderBlock) axiomIterator.next();
                    return new AxiomSoapHeaderElement(axiomHeaderBlock);
                }
                catch (OMException ex) {
                    throw new AxiomSoapHeaderException(ex);
                }
            }

            public void remove() {
                axiomIterator.remove();
            }
        }
    }

    /**
     * Axiom-specific version of <code>org.springframework.ws.soap.SoapHeaderHeaderElement</code>.
     */
    private class AxiomSoapHeaderElement implements SoapHeaderElement {

        private final SOAPHeaderBlock axiomHeaderBlock;

        public AxiomSoapHeaderElement(SOAPHeaderBlock axiomHeaderBlock) {
            this.axiomHeaderBlock = axiomHeaderBlock;
        }

        public QName getName() {
            return axiomHeaderBlock.getQName();
        }

        public Source getSource() {
            try {
                return new StaxSource(axiomHeaderBlock.getXMLStreamReader());
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }

        }

        public String getRole() {
            return axiomHeaderBlock.getRole();
        }

        public void setRole(String role) {
            axiomHeaderBlock.setRole(role);
        }

        public boolean getMustUnderstand() {
            return axiomHeaderBlock.getMustUnderstand();
        }

        public void setMustUnderstand(boolean mustUnderstand) {
            axiomHeaderBlock.setMustUnderstand(mustUnderstand);
        }

        public Result getResult() {
            try {
                return new SAXResult(new AxiomContentHandler(axiomHeaderBlock));
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }

        }

        public void addAttribute(QName name, String value) throws SoapHeaderException {
            try {
                OMNamespace namespace = axiomFactory.createOMNamespace(name.getNamespaceURI(), name.getPrefix());
                OMAttribute attribute = axiomFactory.createOMAttribute(name.getLocalPart(), namespace, value);
                axiomHeaderBlock.addAttribute(attribute);
            }
            catch (OMException ex) {
                throw new AxiomSoapHeaderException(ex);
            }
        }
    }

    /**
     * Axiom-specific version of <code>org.springframework.ws.soap.SoapBody</code>.
     */
    private class AxiomSoapBody implements SoapBody {

        private final SOAPBody axiomBody;

        public AxiomSoapBody(SOAPBody axiomBody) {
            this.axiomBody = axiomBody;
        }

        public Source getPayloadSource() {
            try {
                OMElement payloadElement = getPayloadElement();
                return (payloadElement != null) ? new StaxSource(payloadElement.getXMLStreamReader()) : null;
            }
            catch (OMException ex) {
                throw new AxiomSoapBodyException(ex);
            }
        }

        public Result getPayloadResult() {
            try {
                return new SAXResult(new AxiomContentHandler(axiomBody));
            }
            catch (OMException ex) {
                throw new AxiomSoapBodyException(ex);
            }

        }

        public SoapFault addFault(QName faultCode, String faultString) {
            Assert.hasLength("faultString cannot be empty", faultString);
            if (!StringUtils.hasLength(faultCode.getNamespaceURI()) || (!StringUtils.hasLength(faultCode.getPrefix())))
            {
                throw new IllegalArgumentException("A fully qualified fault code (namespace, prefix, and local part) " +
                        "must be specific for a custom fault code");
            }
            for (Iterator iterator = axiomBody.getChildElements(); iterator.hasNext();) {
                OMElement child = (OMElement) iterator.next();
                child.detach();
            }
            try {
                SOAPFault axiomFault = axiomFactory.createSOAPFault(axiomBody);
                SOAPFaultCode axiomFaultCode = axiomFactory.createSOAPFaultCode(axiomFault);
                SOAPFaultValue axiomFaultValue = axiomFactory.createSOAPFaultValue(axiomFaultCode);
                if (faultCode.getNamespaceURI().equals(axiomFault.getNamespace().getName())) {
                    axiomFaultValue.setText(faultCode);
                }
                else {
                    axiomFaultValue.declareNamespace(faultCode.getNamespaceURI(), faultCode.getPrefix());
                    axiomFaultValue.setText(faultCode.getPrefix() + ":" + faultCode.getLocalPart());
                }
                SOAPFaultReason axiomFaultReason = axiomFactory.createSOAPFaultReason(axiomFault);
                SOAPFaultText axiomFaultText = axiomFactory.createSOAPFaultText(axiomFaultReason);
                axiomFaultText.setText(faultString);
                return new AxiomSoapFault(axiomFault);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }
        }

        public boolean hasFault() {
            return axiomBody.hasFault();
        }

        public SoapFault getFault() {
            SOAPFault axiomFault = axiomBody.getFault();
            return (axiomFault != null) ? new AxiomSoapFault(axiomFault) : null;
        }

        public QName getName() {
            return axiomBody.getQName();
        }

        public Source getSource() {
            try {
                return new StaxSource(axiomBody.getXMLStreamReader());
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public OMElement getPayloadElement() throws OMException {
            try {
                return axiomBody.getFirstElement();
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }
        }
    }

    /**
     * Axiom-specific version of <code>org.springframework.ws.soap.SoapFault</code>.
     */
    private class AxiomSoapFault implements SoapFault {

        private final SOAPFault axiomFault;

        public AxiomSoapFault(SOAPFault axiomFault) {
            this.axiomFault = axiomFault;
        }

        public QName getName() {
            return axiomFault.getQName();
        }

        public Source getSource() {
            return new StaxSource(axiomFault.getXMLStreamReader());
        }

        public QName getFaultCode() {
            try {
                QName qName = axiomFault.getCode().getValue().getTextAsQName();
                int idx = qName.getLocalPart().indexOf(':');
                if (idx != -1) {
                    String prefix = qName.getLocalPart().substring(0, idx);
                    String localPart = qName.getLocalPart().substring(idx + 1);
                    String namespaceUri = axiomFault.getCode().getValue().findNamespaceURI(prefix).getName();
                    qName = new QName(namespaceUri, localPart, prefix);
                }
                return qName;
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public String getFaultString() {
            try {
                SOAPFaultText soapText = axiomFault.getReason().getFirstSOAPText();
                if (soapText != null) {
                    return soapText.getText();
                }
                return null;
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }
        }

        public void setFaultString(String faultString, Locale locale) {
            try {
                SOAPFaultReason axiomFaultReason = axiomFault.getReason();
                Iterator iterator = axiomFaultReason.getAllSoapTexts().iterator();
                while (iterator.hasNext()) {
                    SOAPFaultText child = (SOAPFaultText) iterator.next();
                    child.detach();
                }
                SOAPFaultText axiomFaultText = axiomFactory.createSOAPFaultText(axiomFaultReason);
                axiomFaultText.setText(faultString);
                String xmlLangString = locale.toString().replace('_', '-');
                axiomFaultText.setLang(xmlLangString);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }
        }

        public Locale getFaultStringLocale() {
            try {
                SOAPFaultText soapText = axiomFault.getReason().getFirstSOAPText();
                if (soapText != null) {
                    String xmlLangString = soapText.getLang();
                    if (xmlLangString != null) {
                        String localeString = xmlLangString.replace('-', '_');
                        return StringUtils.parseLocaleString(localeString);
                    }

                }
                return null;
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public String getFaultRole() {
            try {
                SOAPFaultRole axiomFaultRole = axiomFault.getRole();
                return axiomFaultRole != null ? axiomFaultRole.getRoleValue() : null;
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public void setFaultRole(String role) {
            try {
                SOAPFaultRole axiomFaultRole = axiomFactory.createSOAPFaultRole(axiomFault);
                axiomFaultRole.setRoleValue(role);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public SoapFaultDetail getFaultDetail() {
            try {
                SOAPFaultDetail axiomFaultDetail = axiomFault.getDetail();
                return axiomFaultDetail != null ? new AxiomSoapFaultDetail(axiomFaultDetail) : null;
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public SoapFaultDetail addFaultDetail() {
            try {
                SOAPFaultDetail axiomFaultDetail = axiomFactory.createSOAPFaultDetail(axiomFault);
                return new AxiomSoapFaultDetail(axiomFaultDetail);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }
    }

    /**
     * Axiom-specific version of <code>org.springframework.ws.soap.SoapFaultDetail</code>.
     */
    private class AxiomSoapFaultDetail implements SoapFaultDetail {

        private final SOAPFaultDetail axiomFaultDetail;

        public AxiomSoapFaultDetail(SOAPFaultDetail axiomFaultDetail) {
            this.axiomFaultDetail = axiomFaultDetail;
        }

        public SoapFaultDetailElement addFaultDetailElement(QName name) {
            try {
                OMElement element = axiomFactory.createOMElement(name, axiomFaultDetail);
                return new AxiomSoapFaultDetailElement(element);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public Iterator getDetailEntries() {
            return new AxiomSoapFaultDetailElementIterator(axiomFaultDetail.getAllDetailEntries());
        }

        public QName getName() {
            return axiomFaultDetail.getQName();
        }

        public Source getSource() {
            try {
                return new StaxSource(axiomFaultDetail.getXMLStreamReader());
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        private class AxiomSoapFaultDetailElementIterator implements Iterator {

            private final Iterator axiomIterator;

            private AxiomSoapFaultDetailElementIterator(Iterator axiomIterator) {
                this.axiomIterator = axiomIterator;
            }

            public boolean hasNext() {
                return axiomIterator.hasNext();
            }

            public Object next() {
                try {
                    OMElement axiomElement = (OMElement) axiomIterator.next();
                    return new AxiomSoapFaultDetailElement(axiomElement);
                }
                catch (OMException ex) {
                    throw new AxiomSoapFaultException(ex);
                }

            }

            public void remove() {
                axiomIterator.remove();
            }
        }

    }

    /**
     * Axiom-specific version of <code>org.springframework.ws.soap.SoapFaultDetailElement</code>.
     */
    private static class AxiomSoapFaultDetailElement implements SoapFaultDetailElement {

        private final OMElement axiomElement;

        public AxiomSoapFaultDetailElement(OMElement axiomElement) {
            this.axiomElement = axiomElement;
        }

        public Result getResult() {
            try {
                return new SAXResult(new AxiomContentHandler(axiomElement));
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

        }

        public void addText(String text) {
            try {
                axiomElement.setText(text);
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }
        }

        public QName getName() {
            return axiomElement.getQName();
        }

        public Source getSource() {
            try {
                return new StaxSource(axiomElement.getXMLStreamReader());
            }
            catch (OMException ex) {
                throw new AxiomSoapFaultException(ex);
            }

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
            this.iterator = Arrays.asList(attachments.getAllContentIDs()).iterator();
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

    /**
     * Specific SAX ContentHandler that adds the resulting AXIOM OMElement to a specified parent element when
     * <code>endDocument</code> is called. Used for returing <code>SAXResult</code>s from Axiom elements.
     */
    private static class AxiomContentHandler extends SAXOMBuilder {

        private OMElement parentElement = null;

        public AxiomContentHandler(OMElement parentElement) {
            this.parentElement = parentElement;
        }

        public void endDocument() throws SAXException {
            super.endDocument();
            parentElement.addChild(super.getRootElement());
        }
    }

}
