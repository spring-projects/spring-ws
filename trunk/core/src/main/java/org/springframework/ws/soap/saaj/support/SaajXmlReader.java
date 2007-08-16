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
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.Text;

import org.springframework.xml.sax.AbstractXmlReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * SAX <code>XMLReader</code> that reads from a SAAJ <code>Node</code>. Consumes <code>XMLEvents</code> from an
 * <code>XMLEventReader</code>, and calls the corresponding methods on the SAX callback interfaces.
 *
 * @author Arjen Poutsma
 * @see javax.xml.soap.Node
 * @see javax.xml.soap.SOAPElement
 * @since 1.0
 */
public class SaajXmlReader extends AbstractXmlReader {

    private final Node startNode;

    /**
     * Constructs a new instance of the <code>SaajXmlReader</code> that reads from the given <code>Node</code>.
     *
     * @param startNode the SAAJ <code>Node</code> to read from
     */
    public SaajXmlReader(Node startNode) {
        this.startNode = startNode;
    }

    /**
     * Parses the StAX XML reader passed at construction-time.
     * <p/>
     * <strong>Note</strong> that the given <code>InputSource</code> is not read, but ignored.
     *
     * @param ignored is ignored
     * @throws org.xml.sax.SAXException A SAX exception, possibly wrapping a <code>XMLStreamException</code>
     */
    public final void parse(InputSource ignored) throws SAXException {
        parse();
    }

    /**
     * Parses the StAX XML reader passed at construction-time.
     * <p/>
     * <strong>Note</strong> that the given system identifier is not read, but ignored.
     *
     * @param ignored is ignored
     * @throws SAXException A SAX exception, possibly wrapping a <code>XMLStreamException</code>
     */
    public final void parse(String ignored) throws SAXException {
        parse();
    }

    private void parse() throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().startDocument();
        }
        handleNode(startNode);
        if (getContentHandler() != null) {
            getContentHandler().endDocument();
        }
    }

    private void handleNode(Node node) throws SAXException {
        if (node instanceof SOAPElement) {
            handleElement((SOAPElement) node);
        }
        else if (node instanceof Text) {
            Text text = (Text) node;
            handleText(text);
        }
    }

    private void handleText(Text text) throws SAXException {
        if (getContentHandler() != null) {
            char[] ch = text.getValue().toCharArray();
            getContentHandler().characters(ch, 0, ch.length);
        }
    }

    private void handleElement(SOAPElement element) throws SAXException {
        Name elementName = element.getElementName();
        if (getContentHandler() != null) {
            for (Iterator iterator = element.getNamespacePrefixes(); iterator.hasNext();) {
                String prefix = (String) iterator.next();
                String namespaceUri = element.getNamespaceURI(prefix);
                getContentHandler().startPrefixMapping(prefix, namespaceUri);
            }
            getContentHandler()
                    .startElement(elementName.getURI(), elementName.getLocalName(), elementName.getQualifiedName(),
                            getAttributes(element));
        }
        for (Iterator iterator = element.getChildElements(); iterator.hasNext();) {
            Node child = (Node) iterator.next();
            handleNode(child);
        }
        if (getContentHandler() != null) {
            getContentHandler()
                    .endElement(elementName.getURI(), elementName.getLocalName(), elementName.getQualifiedName());
            for (Iterator iterator = element.getNamespacePrefixes(); iterator.hasNext();) {
                String prefix = (String) iterator.next();
                getContentHandler().endPrefixMapping(prefix);
            }
        }
    }

    private Attributes getAttributes(SOAPElement element) {
        AttributesImpl attributes = new AttributesImpl();
        for (Iterator iterator = element.getAllAttributes(); iterator.hasNext();) {
            Name attributeName = (Name) iterator.next();
            String attributeValue = element.getAttributeValue(attributeName);
            attributes.addAttribute(attributeName.getURI(), attributeName.getLocalName(),
                    attributeName.getQualifiedName(), "CDATA", attributeValue);
        }
        return attributes;
    }


}
