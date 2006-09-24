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

package org.springframework.ws.mock.soap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageCreationException;
import org.springframework.ws.soap.SoapMessageNotReadableException;
import org.springframework.ws.soap.SoapMessageNotWritableException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Arjen Poutsma
 */
public class MockSoapMessage implements SoapMessage {

    private static final String URI_NS_SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";

    private static final String MUST_UNDERSTAND_ATTR = "mustUnderstand";

    private Document document;

    private String soapAction;

    private Element envelope;

    private Element header;

    private Element body;

    public MockSoapMessage() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
        }
        catch (ParserConfigurationException e) {
            throw new SoapMessageCreationException("Could not create document builder: " + e.getMessage(), e);
        }
        envelope = document.createElementNS(URI_NS_SOAP_ENVELOPE, "Envelope");
        document.appendChild(envelope);
        body = document.createElementNS(URI_NS_SOAP_ENVELOPE, "Body");
        envelope.appendChild(body);
    }

    public Element getFault() {
        Element payloadElement = getPayloadElement();
        if (payloadElement == null) {
            return null;
        }
        else if (payloadElement.getNamespaceURI().equals(URI_NS_SOAP_ENVELOPE) &&
                payloadElement.getLocalName().equals("Fault")) {
            return payloadElement;
        }
        else {
            return null;
        }
    }

    public Element getHeader() {
        if (header == null) {
            header = document.createElementNS(URI_NS_SOAP_ENVELOPE, "Header");
            envelope.insertBefore(header, body);
        }
        return header;
    }

    public String getHeaderAsString() {
        try {
            Transformer transformer = getTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(getHeader()), new StreamResult(writer));
            return writer.toString();
        }
        catch (TransformerException e) {
            return "";
        }
    }

    public Element[] getHeaderElements() {
        if (header == null) {
            return new Element[0];
        }
        else {
            NodeList nodes = header.getChildNodes();
            Element[] headerElements = new Element[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                headerElements[i] = (Element) nodes.item(i);
            }
            return headerElements;
        }
    }

    public String getPayloadAsString() {
        Source payloadSource = getPayloadSource();
        if (payloadSource == null) {
            return "";
        }
        try {
            Transformer transformer = getTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(payloadSource, new StreamResult(writer));
            return writer.toString();
        }
        catch (TransformerException e) {
            return "";
        }
    }

    private Element getPayloadElement() {
        return (Element) body.getFirstChild();
    }

    public Result getPayloadResult() {
        return new DOMResult(body);
    }

    public Source getPayloadSource() {
        Element bodyElement = getPayloadElement();
        return (bodyElement != null) ? new DOMSource(bodyElement) : null;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    private Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        return transformerFactory.newTransformer();
    }

    public void setPayload(String payload) {
        try {
            Transformer transformer = getTransformer();
            Source source = new StreamSource(new StringReader(payload));
            transformer.transform(source, getPayloadResult());
        }
        catch (TransformerException ex) {
            throw new SoapMessageNotWritableException(ex);
        }
    }

    public Element[] getHeaderElements(QName qName) {
        if (header == null) {
            return new Element[0];
        }
        else {
            NodeList nodes = header.getElementsByTagNameNS(qName.getNamespaceURI(), qName.getLocalPart());
            Element[] headerElements = new Element[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                headerElements[i] = (Element) nodes.item(i);
            }
            return headerElements;
        }
    }

    public Element[] getMustUnderstandHeaderElements(String actor) {
        Element[] headerElements = getHeaderElements();
        List mustUnderstandElements = new ArrayList();
        for (int i = 0; i < headerElements.length; i++) {
            if (isMustUnderstand(headerElements[i])) {
                mustUnderstandElements.add(headerElements[i]);
            }
        }
        return (Element[]) mustUnderstandElements.toArray(new Element[mustUnderstandElements.size()]);
    }

    public Element addFault(QName faultCode, String faultString, String faultActor) {
        Element faultElement = document.createElementNS(URI_NS_SOAP_ENVELOPE, "Fault");
        body.appendChild(faultElement);
        Element faultCodeElement = document.createElementNS(null, "faultcode");
        faultElement.appendChild(faultCodeElement);
        if (StringUtils.hasLength(faultCode.getNamespaceURI()) && StringUtils.hasLength(faultCode.getPrefix())) {
            faultElement.setAttribute("xmlns:" + faultCode.getPrefix(), faultCode.getNamespaceURI());
        }
        faultCodeElement.setTextContent(convertQName(faultCode));
        Element faultStringElement = document.createElementNS(null, "faultstring");
        faultElement.appendChild(faultStringElement);
        faultStringElement.setTextContent(faultString);
        if (StringUtils.hasText(faultActor)) {
            Element faultActorElement = document.createElementNS(null, "faultactor");
            faultElement.appendChild(faultActorElement);
            faultActorElement.setTextContent(faultActor);
        }
        return faultElement;
    }

    public Element addHeaderElement(QName qName, boolean mustUnderstand, String actor) {
        Element headerElement = document.createElementNS(qName.getNamespaceURI(), convertQName(qName));
        getHeader().appendChild(headerElement);
        if (mustUnderstand) {
            headerElement.setAttributeNS(URI_NS_SOAP_ENVELOPE, MUST_UNDERSTAND_ATTR, "1");
        }
        if (StringUtils.hasLength(actor)) {
            headerElement.setAttributeNS(URI_NS_SOAP_ENVELOPE, "actor", actor);
        }
        return headerElement;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        try {
            Transformer transformer = getTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        }
        catch (TransformerException ex) {
            throw new SoapMessageNotReadableException(ex);
        }
    }

    private String convertQName(QName qName) {
        String qualifiedName;
        if (StringUtils.hasLength(qName.getPrefix())) {
            qualifiedName = qName.getPrefix() + ":" + qName.getLocalPart();
        }
        else {
            qualifiedName = qName.getLocalPart();
        }
        return qualifiedName;
    }

    private boolean isMustUnderstand(Element headerElement) {
        if (!headerElement.hasAttributeNS(URI_NS_SOAP_ENVELOPE, MUST_UNDERSTAND_ATTR)) {
            return false;
        }
        else {
            return "1".equals(headerElement.getAttributeNS(URI_NS_SOAP_ENVELOPE, MUST_UNDERSTAND_ATTR));
        }
    }

    public String toString() {
        try {
            Transformer transformer = getTransformer();
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        }
        catch (TransformerException e) {
            return "";
        }
    }
}
