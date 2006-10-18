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
 * SAX <code>XMLReader</code> that can read from a SAAJ <code>SOAPElement</code>
 * @author Arjen Poutsma
 */
public class SoapElementXmlReader extends AbstractXmlReader {

    private SOAPElement element;

    public SoapElementXmlReader(SOAPElement element) {
        this.element = element;
    }

    /**
     * Parses the StAX XML reader passed at construction-time.
     * <p/>
     * <strong>Note</strong> that the given <code>InputSource</code> is not read, but ignored.
     *
     * @param ignored is ignored
     * @throws SAXException A SAX exception, possibly wrapping a <code>XMLStreamException</code>
     */
    public final void parse(InputSource ignored) throws SAXException {
        handleNode(element);
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
        handleNode(element);
    }

    private void handleNode(Node node) throws SAXException {
        if (node instanceof SOAPElement) {
            SOAPElement soapElement = (SOAPElement) node;
            handleSoapElement(soapElement);
        }
        else if (node instanceof Text) {
            Text text = (Text) node;
            handleText(text);
        }
    }

    private void handleText(Text text) throws SAXException {
        if (!text.isComment() && getContentHandler() != null) {
            char[] ch = text.getValue().toCharArray();
            getContentHandler().characters(ch, 0, ch.length);
        }
    }

    private void handleSoapElement(SOAPElement soapElement) throws SAXException {
        if (getContentHandler() != null) {
            for (Iterator i = soapElement.getNamespacePrefixes(); i.hasNext();) {
                String prefix = (String) i.next();
                getContentHandler().startPrefixMapping(prefix, soapElement.getNamespaceURI(prefix));
            }
            Name name = soapElement.getElementName();
            getContentHandler().startElement(name.getURI(), name.getLocalName(), name.getQualifiedName(),
                    getAttributes(soapElement));
            for (Iterator i = soapElement.getChildElements(); i.hasNext();) {
                Node child = (Node) i.next();
                handleNode(child);
            }
            getContentHandler().endElement(name.getURI(), name.getLocalName(), name.getQualifiedName());
        }
    }

    private Attributes getAttributes(SOAPElement soapElement) {
        AttributesImpl attributes = new AttributesImpl();

        for (Iterator i = soapElement.getAllAttributes(); i.hasNext();) {
            Name name = (Name) i.next();
            attributes.addAttribute(name.getURI(), name.getLocalName(), name.getQualifiedName(), null,
                    soapElement.getAttributeValue(name));
        }
        return attributes;
    }
}
