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

package org.springframework.xml.stream;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.springframework.xml.namespace.QNameUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * SAX <code>XMLReader</code> that reads from a StAX <code>XMLStreamReader</code>.  Reads from an
 * <code>XMLStreamReader</code>, and calls the corresponding methods on the SAX callback interfaces.
 *
 * @author Arjen Poutsma
 * @see XMLStreamReader
 * @see #setContentHandler(org.xml.sax.ContentHandler)
 * @see #setDTDHandler(org.xml.sax.DTDHandler)
 * @see #setEntityResolver(org.xml.sax.EntityResolver)
 * @see #setErrorHandler(org.xml.sax.ErrorHandler)
 */
public class StaxStreamXmlReader extends StaxXmlReader {

    private final XMLStreamReader reader;

    /**
     * Constructs a new instance of the <code>StaxStreamXmlReader</code> that reads from the given
     * <code>XMLStreamReader</code>.  The supplied stream reader must be in <code>XMLStreamConstants.START_DOCUMENT</code>
     * or <code>XMLStreamConstants.START_ELEMENT</code> state.
     *
     * @param reader the <code>XMLEventReader</code> to read from
     * @throws IllegalStateException if the reader is not at the start of a document or element
     */
    public StaxStreamXmlReader(XMLStreamReader reader) {
        int event = reader.getEventType();
        if (!(event == XMLStreamConstants.START_DOCUMENT || event == XMLStreamConstants.START_ELEMENT)) {
            throw new IllegalStateException("XMLEventReader not at start of document or element");
        }
        this.reader = reader;
    }

    protected void parseInternal() throws SAXException, XMLStreamException {
        boolean documentEnded = false;
        while (reader.hasNext()) {
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    handleStartElement();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    handleEndElement();
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    handleProcessingInstruction();
                    break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                    handleCharacters();
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    handleStartDocument();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    handleEndDocument();
                    documentEnded = true;
                    break;
            }
            reader.next();
        }
        if (!documentEnded) {
            handleEndDocument();
        }

    }

    private void handleCharacters() throws SAXException {
        if (getContentHandler() != null) {
            if (reader.isWhiteSpace()) {
                getContentHandler()
                        .ignorableWhitespace(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
            }
            else {
                getContentHandler()
                        .characters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
            }
        }
    }

    private void handleEndDocument() throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().endDocument();
        }
    }

    private void handleEndElement() throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().endElement(reader.getName().getNamespaceURI(), reader.getName().getLocalPart(),
                    QNameUtils.toQualifiedName(reader.getName()));

            for (int i = 0; i < reader.getNamespaceCount(); i++) {
                getContentHandler().endPrefixMapping(reader.getNamespacePrefix(i));
            }
        }
    }

    private void handleProcessingInstruction() throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().processingInstruction(reader.getPITarget(), reader.getPIData());
        }
    }

    private void handleStartDocument() throws SAXException {
        setLocator(reader.getLocation());
        if (getContentHandler() != null) {
            getContentHandler().startDocument();
        }
    }

    private void handleStartElement() throws SAXException {
        if (getContentHandler() != null) {
            for (int i = 0; i < reader.getNamespaceCount(); i++) {
                getContentHandler().startPrefixMapping(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
            }

            getContentHandler().startElement(reader.getName().getNamespaceURI(), reader.getName().getLocalPart(),
                    QNameUtils.toQualifiedName(reader.getName()), getAttributes());
        }
    }

    private Attributes getAttributes() {
        AttributesImpl attributes = new AttributesImpl();

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            attributes.addAttribute(reader.getAttributeNamespace(i), reader.getAttributeLocalName(i),
                    QNameUtils.toQualifiedName(reader.getAttributeName(i)), reader.getAttributeType(i),
                    reader.getAttributeValue(i));
        }

        return attributes;
    }


}
