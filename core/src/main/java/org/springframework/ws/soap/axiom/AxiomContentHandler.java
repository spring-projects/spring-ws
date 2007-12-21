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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import org.springframework.util.Assert;
import org.springframework.xml.namespace.QNameUtils;

/**
 * Specific SAX ContentHandler that adds the resulting AXIOM OMElement to a specified parent element when
 * <code>endDocument</code> is called. Used for returing <code>SAXResult</code>s from Axiom elements.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
class AxiomContentHandler implements ContentHandler {

    private final OMFactory factory;

    private final List elements = new ArrayList();

    private Map namespaces = new HashMap();

    private final OMContainer container;

    AxiomContentHandler(OMContainer container, OMFactory factory) {
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
        for (Iterator iterator = namespaces.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String prefix = (String) entry.getKey();
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
        factory.createOMText(parent, data);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        OMContainer parent = getParent();
        factory.createOMProcessingInstruction(parent, target, data);
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

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

}
