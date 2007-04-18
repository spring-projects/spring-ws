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

package org.springframework.ws.soap.saaj;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
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

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.support.SaajContentHandler;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.soap.saaj.support.SaajXmlReader;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.xml.namespace.QNameUtils;
import org.xml.sax.InputSource;

/**
 * SAAJ 1.1 specific implementation of the <code>SaajImplementation</code> interface.
 *
 * @author Arjen Poutsma
 */
public class Saaj11Implementation implements SaajImplementation {

    private static final Saaj11Implementation INSTANCE = new Saaj11Implementation();

    private Saaj11Implementation() {
    }

    public static Saaj11Implementation getInstance() {
        return INSTANCE;
    }

    public QName getName(SOAPElement element) {
        return SaajUtils.toQName(element.getElementName());
    }

    public Source getSource(SOAPElement element) {
        return new SAXSource(new SaajXmlReader(element), new InputSource());
    }

    public Result getResult(SOAPElement element) {
        return new SAXResult(new SaajContentHandler(element));
    }

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

    public DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException {
        Name detailEntryName = SaajUtils.toName(name, detail);
        return detail.addDetailEntry(detailEntryName);
    }

    public SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException {
        Name saajName = SaajUtils.toName(name, header);
        return header.addHeaderElement(saajName);
    }

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
    public SOAPEnvelope getEnvelope(SOAPMessage message) throws SOAPException {
        return message.getSOAPPart().getEnvelope();
    }

    /** Returns the header of the given envelope. */
    public SOAPHeader getHeader(SOAPEnvelope envelope) throws SOAPException {
        return envelope.getHeader();
    }

    /** Returns the body of the given envelope. */
    public SOAPBody getBody(SOAPEnvelope envelope) throws SOAPException {
        return envelope.getBody();
    }

    /** Returns all header elements. */
    public Iterator examineAllHeaderElements(SOAPHeader header) {
        return header.getChildElements();
    }

    /** Returns all header elements for which the must understand attribute is true, given the actor or role. */
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
    public String getActorOrRole(SOAPHeaderElement headerElement) {
        return headerElement.getActor();
    }

    /** Sets the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    public void setActorOrRole(SOAPHeaderElement headerElement, String actorOrRole) {
        headerElement.setActor(actorOrRole);
    }

    /** Gets the must understand attribute for the given header element. */
    public boolean getMustUnderstand(SOAPHeaderElement headerElement) {
        return headerElement.getMustUnderstand();
    }

    /** Sets the must understand attribute for the given header element. */
    public void setMustUnderstand(SOAPHeaderElement headerElement, boolean mustUnderstand) {
        headerElement.setMustUnderstand(mustUnderstand);
    }

    /** Returns <code>true</code> if the body has a fault, <code>false</code> otherwise. */
    public boolean hasFault(SOAPBody body) {
        return body.hasFault();
    }

    /** Returns the fault for the given body, if any. */
    public SOAPFault getFault(SOAPBody body) {
        return body.getFault();
    }

    /** Returns the actor for the given fault. */
    public String getFaultActor(SOAPFault fault) {
        return fault.getFaultActor();
    }

    /** Sets the actor for the given fault. */
    public void setFaultActor(SOAPFault fault, String actorOrRole) throws SOAPException {
        fault.setFaultActor(actorOrRole);
    }

    /** Returns the fault string for the given fault. */
    public String getFaultString(SOAPFault fault) {
        return fault.getFaultString();
    }

    /** Returns the fault string language for the given fault. */
    public Locale getFaultStringLocale(SOAPFault fault) {
        return Locale.ENGLISH;
    }

    /** Returns the fault detail for the given fault. */
    public Detail getFaultDetail(SOAPFault fault) {
        return fault.getDetail();
    }

    /** Adds a fault detail for the given fault. */
    public Detail addFaultDetail(SOAPFault fault) throws SOAPException {
        return fault.addDetail();
    }

    public void addTextNode(DetailEntry detailEntry, String text) throws SOAPException {
        detailEntry.addTextNode(text);
    }

    /** Returns an iteration over all detail entries. */
    public Iterator getDetailEntries(Detail detail) {
        return detail.getDetailEntries();
    }

    public SOAPElement getFirstBodyElement(SOAPBody body) {
        for (Iterator iterator = body.getChildElements(); iterator.hasNext();) {
            Object child = iterator.next();
            if (child instanceof SOAPElement) {
                return (SOAPElement) child;
            }
        }
        return null;
    }

    public void removeContents(SOAPElement element) {
        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {
            iterator.remove();
        }
    }

    public void writeTo(SOAPMessage message, OutputStream outputStream) throws SOAPException, IOException {
        if (message.saveRequired()) {
            message.saveChanges();
        }
        if (outputStream instanceof TransportOutputStream) {
            TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
            // some SAAJ implementations (Axis 1) do not have a Content-Type header by default
            MimeHeaders headers = message.getMimeHeaders();
            if (ObjectUtils.isEmpty(headers.getHeader("Content-Type"))) {
                headers.addHeader("Content-Type", SoapVersion.SOAP_11.getContentType());
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

    public MimeHeaders getMimeHeaders(SOAPMessage message) {
        return message.getMimeHeaders();
    }

    public Iterator getAttachments(SOAPMessage message) {
        return message.getAttachments();
    }

    public Iterator getAttachment(SOAPMessage message, MimeHeaders mimeHeaders) {
        return message.getAttachments(mimeHeaders);
    }

    public AttachmentPart addAttachmentPart(SOAPMessage message, DataSource dataSource) {
        AttachmentPart attachmentPart = message.createAttachmentPart(new DataHandler(dataSource));
        message.addAttachmentPart(attachmentPart);
        return attachmentPart;
    }

    //
    // Unsupported
    //

    public String getFaultRole(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public void setFaultRole(SOAPFault fault, String role) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public Iterator getFaultSubcodes(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public void appendFaultSubcode(SOAPFault fault, QName subcode) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public String getFaultNode(SOAPFault fault) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public void setFaultNode(SOAPFault fault, String uri) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public String getFaultReasonText(SOAPFault fault, Locale locale) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

    public void setFaultReasonText(SOAPFault fault, Locale locale, String text) {
        throw new UnsupportedOperationException("SAAJ 1.1 does not support SOAP 1.2");
    }

}
