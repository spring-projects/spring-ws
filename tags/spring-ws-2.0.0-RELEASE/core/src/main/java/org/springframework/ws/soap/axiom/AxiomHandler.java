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

package org.springframework.ws.soap.axiom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import org.springframework.util.Assert;
import org.springframework.xml.namespace.QNameUtils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Specific SAX {@link ContentHandler} and {@link LexicalHandler} that adds the resulting AXIOM OMElement to a specified
 * parent element when <code>endDocument</code> is called. Used for returing <code>SAXResult</code>s from Axiom
 * elements.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@SuppressWarnings("Since15")
class AxiomHandler implements ContentHandler, LexicalHandler {

    private final OMFactory factory;

    private final List<OMContainer> elements = new ArrayList<OMContainer>();

    private Map<String, String> namespaces = new HashMap<String, String>();

    private final OMContainer container;

    private int charactersType = XMLStreamConstants.CHARACTERS;

    AxiomHandler(OMContainer container, OMFactory factory) {
        Assert.notNull(container, "'container' must not be null");
        Assert.notNull(factory, "'factory' must not be null");
        this.factory = factory;
        this.container = container;
    }

    private OMContainer getParent() {
        if (!elements.isEmpty()) {
            return (OMContainer) elements.get(elements.size() - 1);
        }
        else {
            return container;
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        namespaces.put(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        namespaces.remove(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        OMContainer parent = getParent();
        OMElement element = factory.createOMElement(localName, null, parent);
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            String prefix = entry.getKey();
            if (prefix.length() == 0) {
                element.declareDefaultNamespace((String) entry.getValue());
            }
            else {
                element.declareNamespace((String) entry.getValue(), prefix);
            }
        }
        QName qname = QNameUtils.toQName(uri, qName);
        element.setLocalName(qname.getLocalPart());
        element.setNamespace(element.findNamespace(qname.getNamespaceURI(), qname.getPrefix()));
        for (int i = 0; i < atts.getLength(); i++) {
            QName attrName = QNameUtils.toQName(atts.getURI(i), atts.getQName(i));
            String value = atts.getValue(i);
            if (!atts.getQName(i).startsWith("xmlns")) {
                OMNamespace namespace = factory.createOMNamespace(attrName.getNamespaceURI(), attrName.getPrefix());
                OMAttribute attribute = factory.createOMAttribute(attrName.getLocalPart(), namespace, value);
                element.addAttribute(attribute);
            }
        }

        elements.add(element);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        elements.remove(elements.size() - 1);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        String data = new String(ch, start, length);
        OMContainer parent = getParent();
        factory.createOMText(parent, data, charactersType);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        charactersType = XMLStreamConstants.SPACE;
        characters(ch, start, length);
        charactersType = XMLStreamConstants.CHARACTERS;
    }

    public void processingInstruction(String target, String data) throws SAXException {
        OMContainer parent = getParent();
        factory.createOMProcessingInstruction(parent, target, data);
    }

    public void comment(char ch[], int start, int length) throws SAXException {
        String content = new String(ch, start, length);
        OMContainer parent = getParent();
        factory.createOMComment(parent, content);
    }

    public void startCDATA() throws SAXException {
        charactersType = XMLStreamConstants.CDATA;
    }

    public void endCDATA() throws SAXException {
        charactersType = XMLStreamConstants.CHARACTERS;
    }

    public void startEntity(String name) throws SAXException {
        if (!isPredefinedEntityReference(name)) {
            charactersType = XMLStreamConstants.ENTITY_REFERENCE;
        }
    }

    public void endEntity(String name) throws SAXException {
        charactersType = XMLStreamConstants.CHARACTERS;
    }

    private boolean isPredefinedEntityReference(String name) {
        return "lt".equals(name) || "gt".equals(name) || "amp".equals(name) || "quot".equals(name) ||
                "apos".equals(name);
    }

    /*
    * Unsupported
    */

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void endDTD() throws SAXException {
    }
}
