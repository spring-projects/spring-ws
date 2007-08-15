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

import java.util.Iterator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.xml.namespace.QNameUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * SAX <code>XMLReader</code> that reads from a StAX <code>XMLEventReader</code>. Consumes <code>XMLEvents</code> from
 * an <code>XMLEventReader</code>, and calls the corresponding methods on the SAX callback interfaces.
 *
 * @author Arjen Poutsma
 * @see XMLEventReader
 * @see #setContentHandler(org.xml.sax.ContentHandler)
 * @see #setDTDHandler(org.xml.sax.DTDHandler)
 * @see #setEntityResolver(org.xml.sax.EntityResolver)
 * @see #setErrorHandler(org.xml.sax.ErrorHandler)
 */
public class StaxEventXmlReader extends AbstractStaxXmlReader {

    private final XMLEventReader reader;

    /**
     * Constructs a new instance of the <code>StaxEventXmlReader</code> that reads from the given
     * <code>XMLEventReader</code>. The supplied event reader must be in <code>XMLStreamConstants.START_DOCUMENT</code>
     * or <code>XMLStreamConstants.START_ELEMENT</code> state.
     *
     * @param reader the <code>XMLEventReader</code> to read from
     * @throws IllegalStateException if the reader is not at the start of a document or element
     */
    public StaxEventXmlReader(XMLEventReader reader) {
        try {
            XMLEvent event = reader.peek();
            if (event == null || !(event.isStartDocument() || event.isStartElement())) {
                throw new IllegalStateException("XMLEventReader not at start of document or element");
            }
        }
        catch (XMLStreamException ex) {
            throw new IllegalStateException("Could not read first element: " + ex.getMessage());
        }

        this.reader = reader;
    }

    protected void parseInternal() throws SAXException, XMLStreamException {
        boolean documentStarted = false;
        boolean documentEnded = false;
        int elementDepth = 0;
        XMLEvent event = null;
        while (reader.hasNext() && elementDepth >= 0) {
            event = reader.nextEvent();
            if (!event.isStartDocument() && !event.isEndDocument() && !documentStarted) {
                handleStartDocument();
                documentStarted = true;
            }
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    elementDepth++;
                    handleStartElement(event.asStartElement());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    elementDepth--;
                    if (elementDepth >= 0) {
                        handleEndElement(event.asEndElement());
                    }
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    handleProcessingInstruction((ProcessingInstruction) event);
                    break;
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CDATA:
                    handleCharacters(event.asCharacters());
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    setLocator(event.getLocation());
                    handleStartDocument();
                    documentStarted = true;
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    handleEndDocument();
                    documentEnded = true;
                    break;
                case XMLStreamConstants.NOTATION_DECLARATION:
                    handleNotationDeclaration((NotationDeclaration) event);
                    break;
                case XMLStreamConstants.ENTITY_DECLARATION:
                    handleEntityDeclaration((EntityDeclaration) event);
                    break;
            }
        }
        if (!documentEnded) {
            handleEndDocument();
        }

    }

    private void handleCharacters(Characters characters) throws SAXException {
        if (getContentHandler() != null) {
            if (characters.isIgnorableWhiteSpace()) {
                getContentHandler()
                        .ignorableWhitespace(characters.getData().toCharArray(), 0, characters.getData().length());
            }
            else {
                getContentHandler().characters(characters.getData().toCharArray(), 0, characters.getData().length());
            }
        }
    }

    private void handleEndDocument() throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().endDocument();
        }
    }

    private void handleEndElement(EndElement endElement) throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().endElement(endElement.getName().getNamespaceURI(), endElement.getName().getLocalPart(),
                    QNameUtils.toQualifiedName(endElement.getName()));

            for (Iterator i = endElement.getNamespaces(); i.hasNext();) {
                Namespace namespace = (Namespace) i.next();
                getContentHandler().endPrefixMapping(namespace.getPrefix());
            }
        }
    }

    private void handleNotationDeclaration(NotationDeclaration declaration) throws SAXException {
        if (getDTDHandler() != null) {
            getDTDHandler().notationDecl(declaration.getName(), declaration.getPublicId(), declaration.getSystemId());
        }
    }

    private void handleEntityDeclaration(EntityDeclaration entityDeclaration) throws SAXException {
        if (getDTDHandler() != null) {
            getDTDHandler().unparsedEntityDecl(entityDeclaration.getName(), entityDeclaration.getPublicId(),
                    entityDeclaration.getSystemId(), entityDeclaration.getNotationName());
        }
    }

    private void handleProcessingInstruction(ProcessingInstruction pi) throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().processingInstruction(pi.getTarget(), pi.getData());
        }
    }

    private void handleStartDocument() throws SAXException {
        if (getContentHandler() != null) {
            getContentHandler().startDocument();
        }
    }

    private void handleStartElement(StartElement startElement) throws SAXException {
        if (getContentHandler() != null) {
            for (Iterator i = startElement.getNamespaces(); i.hasNext();) {
                Namespace namespace = (Namespace) i.next();
                getContentHandler().startPrefixMapping(namespace.getPrefix(), namespace.getNamespaceURI());
            }

            getContentHandler().startElement(startElement.getName().getNamespaceURI(),
                    startElement.getName().getLocalPart(), QNameUtils.toQualifiedName(startElement.getName()),
                    getAttributes(startElement));
        }
    }

    private Attributes getAttributes(StartElement event) {
        AttributesImpl attributes = new AttributesImpl();

        for (Iterator i = event.getAttributes(); i.hasNext();) {
            Attribute attribute = (Attribute) i.next();
            attributes.addAttribute(attribute.getName().getNamespaceURI(), attribute.getName().getLocalPart(),
                    QNameUtils.toQualifiedName(attribute.getName()), attribute.getDTDType(), attribute.getValue());
        }

        return attributes;
    }

}
