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

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.support.SaajContentHandler;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.saaj.support.SaajXmlReader;
import org.springframework.ws.transport.TransportConstants;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.xml.namespace.QNameUtils;

/**
 * SAAJ 1.1 specific implementation of the <code>SaajImplementation</code> interface.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class Saaj11Implementation extends SaajImplementation {

    private static final Saaj11Implementation INSTANCE = new Saaj11Implementation();

    private Saaj11Implementation() {
    }

    public static Saaj11Implementation getInstance() {
        return INSTANCE;
    }

    @Override
    public QName getName(SOAPElement element) {
        return SaajUtils.toQName(element.getElementName());
    }

    @Override
    public Source getSource(SOAPElement element) {
        return new SAXSource(new SaajXmlReader(element), new InputSource());
    }

    @Override
    public Result getResult(SOAPElement element) {
        return new SAXResult(new SaajContentHandler(element));
    }

    @Override
    public String getText(SOAPElement element) {
        return element.getValue();
    }

    @Override
    public void setText(SOAPElement element, String content) throws SOAPException {
        element.addTextNode(content);
    }

    @Override
    public void addAttribute(SOAPElement element, QName name, String value) throws SOAPException {
        Name attributeName = SaajUtils.toName(name, element);
        element.addAttribute(attributeName, value);
    }

    @Override
    public void removeAttribute(SOAPElement element, QName name) throws SOAPException {
        Name attributeName = SaajUtils.toName(name, element);
        element.removeAttribute(attributeName);
    }

    @Override
    public String getAttributeValue(SOAPElement element, QName name) throws SOAPException {
        Name attributeName = SaajUtils.toName(name, element);
        return element.getAttributeValue(attributeName);
    }

    @Override
    public Iterator getAllAttibutes(SOAPElement element) {
        List results = new ArrayList();
        for (Iterator iterator = element.getAllAttributes(); iterator.hasNext();) {
            Name attributeName = (Name) iterator.next();
            results.add(SaajUtils.toQName(attributeName));
        }
        return results.iterator();
    }

    @Override
    public QName getFaultCode(SOAPFault fault) {
        String code = fault.getFaultCode();
        int idx = code.indexOf(':');
        if (idx != -1) {
            String prefix = code.substring(0, idx);
            String namespace = fault.getNamespaceURI(prefix);
            if (StringUtils.hasLength(namespace)) {
                return QNameUtils.createQName(namespace, code.substring(idx + 1), prefix);
            }
        }
        return new QName(code);
    }

    public boolean isSoap11(SOAPElement element) {
        return true;
    }

    @Override
    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException {
        Name detailEntryName = SaajUtils.toName(name, detail);
        return detail.addDetailEntry(detailEntryName);
    }

    @Override
    public SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException {
        Name saajName = SaajUtils.toName(name, header);
        return header.addHeaderElement(saajName);
    }

    @Override
    public SOAPFault addFault(SOAPBody body, QName faultCode, String faultString, Locale locale) throws SOAPException {
        SOAPFault fault = body.addFault();
        if (StringUtils.hasLength(faultCode.getNamespaceURI()) &&
                StringUtils.hasLength(QNameUtils.getPrefix(faultCode))) {
            fault.addNamespaceDeclaration(faultCode.getPrefix(), faultCode.getNamespaceURI());
            fault.setFaultCode(faultCode.getPrefix() + ":" + faultCode.getLocalPart());
        }
        else if (faultCode.getNamespaceURI().equals(body.getElementName().getURI())) {
            fault.setFaultCode(body.getElementName().getPrefix() + ":" + faultCode.getLocalPart());
        }
        else {
            fault.setFaultCode(faultCode.getLocalPart());
        }
        fault.setFaultString(faultString);
        return fault;
    }

    /** Returns the envelope of the given message. */
    @Override
    public SOAPEnvelope getEnvelope(SOAPMessage message) throws SOAPException {
        return message.getSOAPPart().getEnvelope();
    }

    /** Returns the header of the given envelope. */
    @Override
    public SOAPHeader getHeader(SOAPEnvelope envelope) throws SOAPException {
        return envelope.getHeader();
    }

    /** Returns the body of the given envelope. */
    @Override
    public SOAPBody getBody(SOAPEnvelope envelope) throws SOAPException {
        return envelope.getBody();
    }

    /** Returns all header elements. */
    @Override
    public Iterator examineAllHeaderElements(SOAPHeader header) {
        return header.getChildElements();
    }

    /** Returns all header elements for which the must understand attribute is true, given the actor or role. */
    @Override
    public Iterator examineMustUnderstandHeaderElements(SOAPHeader header, String actorOrRole) {
        List result = new ArrayList();
        for (Iterator iterator = header.examineHeaderElements(actorOrRole); iterator.hasNext();) {
            SOAPHeaderElement headerElement = (SOAPHeaderElement) iterator.next();
            if (headerElement.getMustUnderstand()) {
                result.add(headerElement);
            }
        }
        return result.iterator();
    }

    /** Returns the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    @Override
    public String getActorOrRole(SOAPHeaderElement headerElement) {
        return headerElement.getActor();
    }

    /** Sets the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    @Override
    public void setActorOrRole(SOAPHeaderElement headerElement, String actorOrRole) {
        headerElement.setActor(actorOrRole);
    }

    /** Gets the must understand attribute for the given header element. */
    @Override
    public boolean getMustUnderstand(SOAPHeaderElement headerElement) {
        return headerElement.getMustUnderstand();
    }

    /** Sets the must understand attribute for the given header element. */
    @Override
    public void setMustUnderstand(SOAPHeaderElement headerElement, boolean mustUnderstand) {
        headerElement.setMustUnderstand(mustUnderstand);
    }

    /** Returns <code>true</code> if the body has a fault, <code>false</code> otherwise. */
    @Override
    public boolean hasFault(SOAPBody body) {
        return body.hasFault();
    }

    /** Returns the fault for the given body, if any. */
    @Override
    public SOAPFault getFault(SOAPBody body) {
        return body.getFault();
    }

    /** Returns the actor for the given fault. */
    @Override
    public String getFaultActor(SOAPFault fault) {
        return fault.getFaultActor();
    }

    /** Sets the actor for the given fault. */
    @Override
    public void setFaultActor(SOAPFault fault, String actorOrRole) throws SOAPException {
        fault.setFaultActor(actorOrRole);
    }

    /** Returns the fault string for the given fault. */
    @Override
    public String getFaultString(SOAPFault fault) {
        return fault.getFaultString();
    }

    /** Returns the fault string language for the given fault. */
    @Override
    public Locale getFaultStringLocale(SOAPFault fault) {
        return Locale.ENGLISH;
    }

    /** Returns the fault detail for the given fault. */
    @Override
    public Detail getFaultDetail(SOAPFault fault) {
        return fault.getDetail();
    }

    /** Adds a fault detail for the given fault. */
    @Override
    public Detail addFaultDetail(SOAPFault fault) throws SOAPException {
        return fault.addDetail();
    }

    @Override
    public void addTextNode(DetailEntry detailEntry, String text) throws SOAPException {
        detailEntry.addTextNode(text);
    }

    /** Returns an iteration over all detail entries. */
    @Override
    public Iterator getDetailEntries(Detail detail) {
        return detail.getDetailEntries();
    }

    @Override
    public SOAPElement getFirstBodyElement(SOAPBody body) {
        for (Iterator iterator = body.getChildElements(); iterator.hasNext();) {
            Object child = iterator.next();
            if (child instanceof SOAPElement) {
                return (SOAPElement) child;
            }
        }
        return null;
    }

    @Override
    public void removeContents(SOAPElement element) {
        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {
            iterator.next();
            iterator.remove();
        }
    }

    @Override
    Iterator getChildElements(SOAPElement element, QName name) throws SOAPException {
        Name elementName = SaajUtils.toName(name, element);
        return element.getChildElements(elementName);
    }

    @Override
    void addNamespaceDeclaration(SOAPElement element, String prefix, String namespaceUri) throws SOAPException {
        element.addNamespaceDeclaration(prefix, namespaceUri);
    }

    @Override
    public void writeTo(SOAPMessage message, OutputStream outputStream) throws SOAPException, IOException {
        if (message.saveRequired()) {
            message.saveChanges();
        }
        if (outputStream instanceof TransportOutputStream) {
            TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
            // some SAAJ implementations (Axis 1) do not have a Content-Type header by default
            MimeHeaders headers = message.getMimeHeaders();
            if (ObjectUtils.isEmpty(headers.getHeader(TransportConstants.HEADER_CONTENT_TYPE))) {
                headers.addHeader(TransportConstants.HEADER_CONTENT_TYPE, SoapVersion.SOAP_11.getContentType());
                if (message.saveRequired()) {
                    message.saveChanges();
                }
            }
            for (Iterator iterator = headers.getAllHeaders(); iterator.hasNext();) {
                MimeHeader mimeHeader = (MimeHeader) iterator.next();
                transportOutputStream.addHeader(mimeHeader.getName(), mimeHeader.getValue());
            }
        }
        message.writeTo(outputStream);

    }

    @Override
    public MimeHeaders getMimeHeaders(SOAPMessage message) {
        return message.getMimeHeaders();
    }

    @Override
    public Iterator getAttachments(SOAPMessage message) {
        return message.getAttachments();
    }

    @Override
    public Iterator getAttachment(SOAPMessage message, MimeHeaders mimeHeaders) {
        return message.getAttachments(mimeHeaders);
    }

    @Override
    public AttachmentPart addAttachmentPart(SOAPMessage message, DataHandler dataHandler) {
        AttachmentPart attachmentPart = message.createAttachmentPart(dataHandler);
        message.addAttachmentPart(attachmentPart);
        return attachmentPart;
    }

    //
    // Unsupported
    //

    @Override
    public String getFaultRole(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public void setFaultRole(SOAPFault fault, String role) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public Iterator getFaultSubcodes(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public void appendFaultSubcode(SOAPFault fault, QName subcode) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public String getFaultNode(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public void setFaultNode(SOAPFault fault, String uri) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public String getFaultReasonText(SOAPFault fault, Locale locale) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    @Override
    public void setFaultReasonText(SOAPFault fault, Locale locale, String text) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

}
