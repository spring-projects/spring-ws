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
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
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

import org.springframework.util.ObjectUtils;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * Forms bridge between the <code>SoapMessage</code> hierarchy and specific version of SAAJ. Various <code>Saaj*</code>
 * classes delegate to this implementation to remain independent of SAAJ versions.
 *
 * @author Arjen Poutsma
 */
abstract class SaajImplementation {

    protected SaajImplementation() {
    }

    /**
     * Returns the singleton instance for the version of SAAJ in use.
     */
    public static SaajImplementation getImplementation() {
        if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_12) {
            return Saaj12Implementation.getInstance();
        }
        else if (SaajUtils.getSaajVersion() == SaajUtils.SAAJ_13) {
            return Saaj13Implementation.getInstance();
        }
        else {
            throw new IllegalStateException("Could not find SAAJ 1.2 or SAAJ 1.3 on the classpath");
        }
    }

    public abstract QName getName(SOAPElement element);

    public Source getSource(SOAPElement element) {
        return new DOMSource(element);
    }

    public Result getResult(SOAPElement element) {
        return new DOMResult(element);
    }

    public SOAPEnvelope getEnvelope(SOAPMessage message) throws SOAPException {
        return message.getSOAPPart().getEnvelope();
    }

    public SOAPHeader getHeader(SOAPEnvelope envelope) throws SOAPException {
        return envelope.getHeader();
    }

    public SOAPBody getBody(SOAPEnvelope envelope) throws SOAPException {
        return envelope.getBody();
    }

    public String getActorOrRole(SOAPHeaderElement headerElement) {
        return headerElement.getActor();
    }

    public void setActorOrRole(SOAPHeaderElement headerElement, String actorOrRole) {
        headerElement.setActor(actorOrRole);
    }

    public boolean getMustUnderstand(SOAPHeaderElement headerElement) {
        return headerElement.getMustUnderstand();
    }

    public void setMustUnderstand(SOAPHeaderElement headerElement, boolean mustUnderstand) {
        headerElement.setMustUnderstand(mustUnderstand);
    }

    public abstract QName getFaultCode(SOAPFault fault);

    public String getFaultActor(SOAPFault fault) {
        return fault.getFaultActor();
    }

    public void setFaultActor(SOAPFault fault, String actorOrRole) throws SOAPException {
        fault.setFaultActor(actorOrRole);
    }

    public void addTextNode(DetailEntry detailEntry, String text) throws SOAPException {
        detailEntry.addTextNode(text);
    }

    public Iterator getDetailEntries(Detail detail) {
        return detail.getDetailEntries();
    }

    public SOAPBodyElement getFirstBodyElement(SOAPBody body) {
        for (Iterator iterator = body.getChildElements(); iterator.hasNext();) {
            Object child = iterator.next();
            if (child instanceof SOAPBodyElement) {
                return (SOAPBodyElement) child;
            }
        }
        return null;
    }

    public boolean hasFault(SOAPBody body) {
        return body.hasFault();
    }

    public SOAPFault getFault(SOAPBody body) {
        return body.getFault();
    }

    public abstract boolean isSoap11(SOAPElement element);

    public void removeContents(SOAPElement element) {
        element.removeContents();
    }

    public abstract DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException;

    public Iterator examineAllHeaderElements(SOAPHeader header) {
        return header.examineAllHeaderElements();
    }

    public Iterator examineMustUnderstandHeaderElements(SOAPHeader header, String actorOrRole) {
        return header.examineMustUnderstandHeaderElements(actorOrRole);
    }

    public abstract SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    public abstract String getFaultRole(SOAPFault fault);

    public abstract void setFaultRole(SOAPFault fault, String role) throws SOAPException;

    public abstract SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    public abstract SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris)
            throws SOAPException;

    public Detail getFaultDetail(SOAPFault fault) {
        return fault.getDetail();
    }

    public Detail addFaultDetail(SOAPFault fault) throws SOAPException {
        return fault.addDetail();
    }

    public String getFaultString(SOAPFault fault) {
        return fault.getFaultString();
    }

    public Locale getFaultStringLocale(SOAPFault fault) {
        return fault.getFaultStringLocale();
    }

    public abstract Iterator getFaultSubcodes(SOAPFault fault);

    public abstract void appendFaultSubcode(SOAPFault fault, QName subcode) throws SOAPException;

    public abstract String getFaultNode(SOAPFault fault);

    public abstract void setFaultNode(SOAPFault fault, String uri) throws SOAPException;

    public abstract String getFaultReasonText(SOAPFault fault, Locale locale) throws SOAPException;

    public abstract void setFaultReasonText(SOAPFault fault, Locale locale, String text) throws SOAPException;

    public abstract SOAPFault addFault(SOAPBody body, QName faultCode, String faultString, Locale locale)
            throws SOAPException;

    public void writeTo(SOAPMessage message, OutputStream outputStream) throws SOAPException, IOException {
        if (message.saveRequired()) {
            message.saveChanges();
        }
        if (outputStream instanceof TransportOutputStream) {
            TransportOutputStream transportOutputStream = (TransportOutputStream) outputStream;
            // some SAAJ implementations (Axis 1) do not have a Content-Type header by default
            MimeHeaders headers = message.getMimeHeaders();
            if (ObjectUtils.isEmpty(headers.getHeader("Content-Type"))) {
                if (isSoap11(getEnvelope(message))) {
                    headers.addHeader("Content-Type", SoapVersion.SOAP_11.getContentType());
                }
                else {
                    headers.addHeader("Content-Type", SoapVersion.SOAP_12.getContentType());
                }
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
}
