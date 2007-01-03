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

package org.springframework.ws.soap.saaj.support;

import java.util.Iterator;
import java.util.Map;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;

import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX <code>ContentHandler</code> that transforms callback calls to the creation of SAAJ <code>Node</code>s and
 * <code>SOAPElement</code>s.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.Node
 * @see javax.xml.soap.SOAPElement
 */
public class SaajContentHandler implements ContentHandler {

    private SOAPElement element;

    private final SOAPEnvelope envelope;

    private Map namespaces = CollectionFactory.createLinkedMapIfPossible(5);

    /**
     * Constructs a new instance of the <code>SaajContentHandler</code> that creates children of the given
     * <code>SOAPElement</code>.
     *
     * @param element the element to write to
     */
    public SaajContentHandler(SOAPElement element) {
        Assert.notNull(element, "element must not be null");
        if (element instanceof SOAPEnvelope) {
            envelope = (SOAPEnvelope) element;
        }
        else {
            envelope = SaajUtils.getEnvelope(element);
        }
        this.element = element;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        try {
            String text = new String(ch, start, length);
            element.addTextNode(text);
        }
        catch (SOAPException ex) {
            throw new SAXException(ex);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            String childPrefix = getPrefix(qName);
            SOAPElement child = element.addChildElement(localName, childPrefix, uri);
            for (int i = 0; i < atts.getLength(); i++) {
                String attributePrefix = getPrefix(atts.getQName(i));
                if (!"xmlns".equals(atts.getLocalName(i))) {
                    Name attributeName = envelope.createName(atts.getLocalName(i), attributePrefix, atts.getURI(i));
                    child.addAttribute(attributeName, atts.getValue(i));
                }
            }
            for (Iterator iterator = namespaces.keySet().iterator(); iterator.hasNext();) {
                String namespacePrefix = (String) iterator.next();
                String namespaceUri = (String) namespaces.get(namespacePrefix);
                child.addNamespaceDeclaration(namespacePrefix, namespaceUri);
            }
            element = child;
        }
        catch (SOAPException ex) {
            throw new SAXException(ex);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        Assert.isTrue(localName.equals(element.getElementName().getLocalName()), "Invalid element on stack");
        Assert.isTrue(uri.equals(element.getElementName().getURI()), "Invalid element on stack");
        element = element.getParentElement();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaces.put(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        namespaces.remove(prefix);
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String data) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    private String getPrefix(String qName) {
        int idx = qName.indexOf(':');
        if (idx != -1) {
            return qName.substring(0, idx);
        }
        else {
            return null;
        }
    }
}
