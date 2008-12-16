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
import java.util.Iterator;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MimeHeaders;
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

/**
 * Forms a bridge between the SOAP class hierarchy and a specific version of SAAJ.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
abstract class SaajImplementation {

    /*
     * SOAPElement
     */

    /** Returns the name of the given element. */
    abstract QName getName(SOAPElement element);

    /** Returns the readable <code>Source</code> of the given element. */
    abstract Source getSource(SOAPElement element);

    /** Returns the writable <code>Result</code> of the given element. */
    abstract Result getResult(SOAPElement element);

    /** Returns the text of the given element */
    abstract String getText(SOAPElement element);

    /** Returns the text of the given element */
    abstract void setText(SOAPElement element, String content) throws SOAPException;

    /** Adds an attribute to the specified element. */
    abstract void addAttribute(SOAPElement element, QName name, String value) throws SOAPException;

    /** Removes an attribute from the specified element. */
    abstract void removeAttribute(SOAPElement element, QName name) throws SOAPException;

    /** Returns the attribute value * */
    abstract String getAttributeValue(SOAPElement element, QName name) throws SOAPException;

    /** Returns all attributes as an iterator of QNames. * */
    abstract Iterator getAllAttibutes(SOAPElement element);

    /** Removes the contents (i.e. children) of the element. */
    abstract void removeContents(SOAPElement element);

    /** Returns an iterator over all the child elements with the specified name. */
    abstract Iterator getChildElements(SOAPElement element, QName name) throws SOAPException;

    /** Declares a namespace. */
    abstract void addNamespaceDeclaration(SOAPElement element, String prefix, String namespaceUri) throws SOAPException;

    /*
     * SOAPMessage
     */

    /** Returns the envelope of the given message. */
    abstract SOAPEnvelope getEnvelope(SOAPMessage message) throws SOAPException;

    /** Writes the given message to the given stream. */
    abstract void writeTo(SOAPMessage message, OutputStream outputStream) throws SOAPException, IOException;

    /** Returns the MIME headers of the message. */
    abstract MimeHeaders getMimeHeaders(SOAPMessage message);

    /** Returns an iteration over all attachments in the message. */
    abstract Iterator getAttachments(SOAPMessage message);

    /** Returns an iteration over all attachments in the message with the given headers. */
    abstract Iterator getAttachment(SOAPMessage message, MimeHeaders mimeHeaders);

    /** Adds an attachment to the given message. */
    abstract AttachmentPart addAttachmentPart(SOAPMessage message, DataHandler dataHandler);

    /*
     * SOAPEnvelope
     */

    /** Returns the header of the given envelope. */
    abstract SOAPHeader getHeader(SOAPEnvelope envelope) throws SOAPException;

    /** Returns the body of the given envelope. */
    abstract SOAPBody getBody(SOAPEnvelope envelope) throws SOAPException;

    /*
     * SOAPHeader
     */

    /** Adds a header element to the given header. */
    abstract SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    /** Returns all header elements. */
    abstract Iterator examineAllHeaderElements(SOAPHeader header);

    /** Returns all header elements for which the must understand attribute is true, given the actor or role. */
    abstract Iterator examineMustUnderstandHeaderElements(SOAPHeader header, String actorOrRole);

    /** Adds a not understood header element to the given header. */
    abstract SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    /** Adds a upgrade header element to the given header. */
    abstract SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris)
            throws SOAPException;

    /*
     * SOAPHeaderElement
     */

    /** Returns the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    abstract String getActorOrRole(SOAPHeaderElement headerElement);

    /** Sets the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    abstract void setActorOrRole(SOAPHeaderElement headerElement, String actorOrRole);

    /** Gets the must understand attribute for the given header element. */
    abstract boolean getMustUnderstand(SOAPHeaderElement headerElement);

    /** Sets the must understand attribute for the given header element. */
    abstract void setMustUnderstand(SOAPHeaderElement headerElement, boolean mustUnderstand);

    /*
     * SOAPBody
     */

    /** Returns <code>true</code> if the body has a fault, <code>false</code> otherwise. */
    abstract boolean hasFault(SOAPBody body);

    /** Returns the fault for the given body, if any. */
    abstract SOAPFault getFault(SOAPBody body);

    /** Adds a fault to the given body. */
    abstract SOAPFault addFault(SOAPBody body, QName faultCode, String faultString, Locale locale) throws SOAPException;

    /** Returns the first child element of the given body. */
    abstract SOAPElement getFirstBodyElement(SOAPBody body);

    /*
     * SOAPFault
     */

    /** Returns the fault code for the given fault. */
    abstract QName getFaultCode(SOAPFault fault);

    /** Returns the actor for the given fault. */
    abstract String getFaultActor(SOAPFault fault);

    /** Sets the actor for the given fault. */
    abstract void setFaultActor(SOAPFault fault, String actorOrRole) throws SOAPException;

    /** Returns the fault string for the given fault. */
    abstract String getFaultString(SOAPFault fault);

    /** Returns the fault string language for the given fault. */
    abstract Locale getFaultStringLocale(SOAPFault fault);

    /** Returns the fault detail for the given fault. */
    abstract Detail getFaultDetail(SOAPFault fault);

    /** Adds a fault detail for the given fault. */
    abstract Detail addFaultDetail(SOAPFault fault) throws SOAPException;

    /** Returns the fault role. */
    abstract String getFaultRole(SOAPFault fault);

    /** Sets the fault role. */
    abstract void setFaultRole(SOAPFault fault, String role) throws SOAPException;

    /** Returns the fault sub code. */
    abstract Iterator getFaultSubcodes(SOAPFault fault);

    /** Adds a fault sub code. */
    abstract void appendFaultSubcode(SOAPFault fault, QName subcode) throws SOAPException;

    /** Returns the fault node. */
    abstract String getFaultNode(SOAPFault fault);

    /** Sets the fault node. */
    abstract void setFaultNode(SOAPFault fault, String uri) throws SOAPException;

    /** Returns the fault reason text. */
    abstract String getFaultReasonText(SOAPFault fault, Locale locale) throws SOAPException;

    /** Sets the fault reason text. */
    abstract void setFaultReasonText(SOAPFault fault, Locale locale, String text) throws SOAPException;

    /*
     * Detail
     */

    /** Adds a detail entry to the given detail. */
    abstract DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException;

    /** Returns an iteration over all detail entries. */
    abstract Iterator getDetailEntries(Detail detail);

    /*
     * DetailEntry
     */

    /** Adds a text node to the given detail entry. */
    abstract void addTextNode(DetailEntry detailEntry, String text) throws SOAPException;
}
