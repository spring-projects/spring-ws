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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
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
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class for easy population of a <code>javax.xml.soap.SOAPMessage</code>.
 * <p/>
 * Mirrors the methods of <code>SoapMessage</code>, directly applying the values to the underlying
 * <code>SOAPMessage</code>, but throws the checked <code>SOAPException</code>.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.SoapMessage
 * @see SOAPMessage
 * @see SOAPException
 */
public class SaajMessageHelper {

    private final SOAPMessage message;

    private static final String MIME_HEADER_SOAP_ACTION = "SOAPAction";

    public SaajMessageHelper(SOAPMessage saajMessage) {
        this.message = saajMessage;
    }

    /**
     * Returns the underlying <code>SOAPMessage</code> object.
     */
    public final SOAPMessage getSaajMessage() {
        return message;
    }

    /**
     * Retrieves the payload of the given SAAJ message as a single DOM element. The payload of a message is the contents
     * of the SOAP body.
     *
     * @return the message payload
     * @throws SOAPException when the message payload cannot be retrieved
     */
    public Element getPayloadElement() throws SOAPException {
        SOAPBody body = message.getSOAPBody();
        return (Element) body.getFirstChild();
    }

    public Source getPayloadSource() throws SOAPException {
        Element bodyElement = getPayloadElement();
        return (bodyElement != null) ? new DOMSource(bodyElement) : null;
    }

    public Result getPayloadResult() throws SOAPException {
        return new DOMResult(message.getSOAPBody());
    }

    public boolean hasFault() throws SOAPException {
        return message.getSOAPBody().hasFault();
    }

    public Element getFault() throws SOAPException {
        return message.getSOAPBody().getFault();
    }

    public Element getHeader() throws SOAPException {
        return message.getSOAPHeader();
    }

    public Element[] getHeaderElements() throws SOAPException {
        return convertToElementArray(getHeader().getChildNodes());
    }

    private Element[] convertToElementArray(NodeList nodes) {
        List elements = new ArrayList();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) nodes.item(i));
            }
        }
        return (Element[]) elements.toArray(new Element[elements.size()]);
    }

    public String getSoapAction() {
        String[] values = message.getMimeHeaders().getHeader(MIME_HEADER_SOAP_ACTION);
        return (ObjectUtils.isEmpty(values)) ? null : values[0];
    }

    public Element addFault(QName faultCode, String faultString, String faultActor) throws SOAPException {
        SOAPFault fault = message.getSOAPBody().addFault(convertQNameToName(faultCode), faultString);
        if (StringUtils.hasLength(faultActor)) {
            fault.setFaultActor(faultActor);
        }
        return fault;
    }

    public void writeTo(OutputStream outputStream) throws IOException, SOAPException {
        message.writeTo(outputStream);
    }

    private Name convertQNameToName(QName qName) throws SOAPException {
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        if (StringUtils.hasLength(qName.getNamespaceURI()) && StringUtils.hasLength(qName.getPrefix())) {
            return envelope.createName(qName.getLocalPart(), qName.getPrefix(), qName.getNamespaceURI());
        }
        else if (StringUtils.hasLength(qName.getNamespaceURI())) {
            String prefix = envelope.lookupPrefix(qName.getNamespaceURI());
            return envelope.createName(qName.getLocalPart(), prefix, qName.getNamespaceURI());
        }
        else {
            return envelope.createName(qName.getLocalPart(), envelope.getPrefix(), envelope.getNamespaceURI());
        }
    }

    public Element addHeaderElement(QName qName, boolean mustUnderstand, String actor) throws SOAPException {
        SOAPHeaderElement headerElement = message.getSOAPHeader().addHeaderElement(convertQNameToName(qName));
        if (mustUnderstand) {
            headerElement.setMustUnderstand(true);
        }
        if (StringUtils.hasLength(actor)) {
            headerElement.setActor(actor);
        }
        return headerElement;
    }

    public Element[] getMustUnderstandHeaderElements(String role) throws SOAPException {
        SOAPHeader header = message.getSOAPHeader();
        if (header == null) {
            return new Element[0];
        }
        else {
            Iterator iterator = header.examineMustUnderstandHeaderElements(role);
            List elements = new ArrayList();
            while (iterator.hasNext()) {
                SOAPHeaderElement element = (SOAPHeaderElement) iterator.next();
                elements.add(element);
            }
            return (Element[]) elements.toArray(new Element[elements.size()]);
        }
    }

    public Element[] getHeaderElements(QName qName) throws SOAPException {
        NodeList nodes = message.getSOAPHeader().getElementsByTagNameNS(qName.getNamespaceURI(), qName.getLocalPart());
        return convertToElementArray(nodes);
    }
}
