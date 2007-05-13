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
 */
public interface SaajImplementation {

    /** Returns the name of the given element. */
    QName getName(SOAPElement element);

    /** Returns the readable <code>Source</code> of the given element. */
    Source getSource(SOAPElement element);

    /** Returns the writable <code>Result</code> of the given element. */
    Result getResult(SOAPElement element);

    /** Adds an attribute to the specified element. */
    void addAttribute(SOAPElement element, QName name, String value) throws SOAPException;

    /** Returns the attribute value * */
    String getAttributeValue(SOAPElement element, QName name) throws SOAPException;

    /** Returns all attributes as an iterator of QNames. * */
    Iterator getAllAttibutes(SOAPElement element);

    /** Returns an iterator over the child elements with the given name. */
    Iterator getChildElements(SOAPElement element, QName name) throws SOAPException;

    /** Returns the envelope of the given message. */
    SOAPEnvelope getEnvelope(SOAPMessage message) throws SOAPException;

    /** Returns the header of the given envelope. */
    SOAPHeader getHeader(SOAPEnvelope envelope) throws SOAPException;

    /** Returns the body of the given envelope. */
    SOAPBody getBody(SOAPEnvelope envelope) throws SOAPException;

    /** Adds a header element to the given header. */
    SOAPHeaderElement addHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    /** Returns all header elements. */
    Iterator examineAllHeaderElements(SOAPHeader header);

    /** Returns all header elements for which the must understand attribute is true, given the actor or role. */
    Iterator examineMustUnderstandHeaderElements(SOAPHeader header, String actorOrRole);

    /** Returns the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    String getActorOrRole(SOAPHeaderElement headerElement);

    /** Sets the SOAP 1.1 actor or SOAP 1.2 role attribute for the given header element. */
    void setActorOrRole(SOAPHeaderElement headerElement, String actorOrRole);

    /** Gets the must understand attribute for the given header element. */
    boolean getMustUnderstand(SOAPHeaderElement headerElement);

    /** Sets the must understand attribute for the given header element. */
    void setMustUnderstand(SOAPHeaderElement headerElement, boolean mustUnderstand);

    /** Returns <code>true</code> if the body has a fault, <code>false</code> otherwise. */
    boolean hasFault(SOAPBody body);

    /** Returns the fault for the given body, if any. */
    SOAPFault getFault(SOAPBody body);

    /** Adds a fault to the given body. */
    SOAPFault addFault(SOAPBody body, QName faultCode, String faultString, Locale locale) throws SOAPException;

    /** Returns the fault code for the given fault. */
    QName getFaultCode(SOAPFault fault);

    /** Returns the actor for the given fault. */
    String getFaultActor(SOAPFault fault);

    /** Sets the actor for the given fault. */
    void setFaultActor(SOAPFault fault, String actorOrRole) throws SOAPException;

    /** Returns the fault string for the given fault. */
    String getFaultString(SOAPFault fault);

    /** Returns the fault string language for the given fault. */
    Locale getFaultStringLocale(SOAPFault fault);

    /** Adds a detail entry to the given detail. */
    DetailEntry addDetailEntry(Detail detail, QName name) throws SOAPException;

    /** Returns the fault detail for the given fault. */
    Detail getFaultDetail(SOAPFault fault);

    /** Adds a fault detail for the given fault. */
    Detail addFaultDetail(SOAPFault fault) throws SOAPException;

    void addTextNode(DetailEntry detailEntry, String text) throws SOAPException;

    /** Returns an iteration over all detail entries. */
    Iterator getDetailEntries(Detail detail);

    /** Returns the first child element of the given body. */
    SOAPElement getFirstBodyElement(SOAPBody body);

    /** Removes the contents (i.e. children) of the element. */
    void removeContents(SOAPElement element);

    /** Writes the given message to the given stream. */
    void writeTo(SOAPMessage message, OutputStream outputStream) throws SOAPException, IOException;

    /** Returns the MIME headers of the message. */
    MimeHeaders getMimeHeaders(SOAPMessage message);

    /** Returns an iteration over all attachments in the message. */
    Iterator getAttachments(SOAPMessage message);

    /** Returns an iteration over all attachments in the message with the given headers. */
    Iterator getAttachment(SOAPMessage message, MimeHeaders mimeHeaders);

    /** Adds an attachment to the given message. */
    AttachmentPart addAttachmentPart(SOAPMessage message, DataHandler dataHandler);

    /** Adds a not understood header element to the given header. */
    SOAPHeaderElement addNotUnderstoodHeaderElement(SOAPHeader header, QName name) throws SOAPException;

    /** Adds a upgrade header element to the given header. */
    SOAPHeaderElement addUpgradeHeaderElement(SOAPHeader header, String[] supportedSoapUris) throws SOAPException;

    /** Returns the fault role. */
    String getFaultRole(SOAPFault fault);

    /** Sets the fault role. */
    void setFaultRole(SOAPFault fault, String role) throws SOAPException;

    /** Returns the fault sub code. */
    Iterator getFaultSubcodes(SOAPFault fault);

    /** Adds a fault sub code. */
    void appendFaultSubcode(SOAPFault fault, QName subcode) throws SOAPException;

    /** Returns the fault node. */
    String getFaultNode(SOAPFault fault);

    /** Sets the fault node. */
    void setFaultNode(SOAPFault fault, String uri) throws SOAPException;

    /** Returns the fault reason text. */
    String getFaultReasonText(SOAPFault fault, Locale locale) throws SOAPException;

    /** Sets the fault reason text. */
    void setFaultReasonText(SOAPFault fault, Locale locale, String text) throws SOAPException;

}
