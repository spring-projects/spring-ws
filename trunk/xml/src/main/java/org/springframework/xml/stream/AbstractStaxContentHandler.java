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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract base class for SAX <code>ContentHandler</code> implementations that use StAX as a basis. All methods
 * delegate to internal template methods, capable of throwing a <code>XMLStreamException</code>. Additionally, an
 * namespace context is used to keep track of declared namespaces.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractStaxContentHandler implements ContentHandler {

    private SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();

    /**
     * @throws SAXException
     */
    public final void startDocument() throws SAXException {
        namespaceContext.clear();
        try {
            startDocumentInternal();
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle startDocument: " + ex.getMessage(), ex);
        }
    }

    protected abstract void startDocumentInternal() throws XMLStreamException;

    public final void endDocument() throws SAXException {
        namespaceContext.clear();
        try {
            endDocumentInternal();
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle startDocument: " + ex.getMessage(), ex);
        }
    }

    protected abstract void endDocumentInternal() throws XMLStreamException;

    /**
     * Binds the given prefix to the given namespaces.
     *
     * @see SimpleNamespaceContext#bindNamespaceUri(String,String)
     */
    public final void startPrefixMapping(String prefix, String uri) {
        namespaceContext.bindNamespaceUri(prefix, uri);
    }

    /**
     * Removes the binding for the given prefix.
     *
     * @see SimpleNamespaceContext#removeBinding(String)
     */
    public final void endPrefixMapping(String prefix) {
        namespaceContext.removeBinding(prefix);
    }

    public final void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            startElementInternal(QNameUtils.toQName(uri, qName), atts, namespaceContext);
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle startElement: " + ex.getMessage(), ex);
        }
    }

    protected abstract void startElementInternal(QName name, Attributes atts, SimpleNamespaceContext namespaceContext)
            throws XMLStreamException;

    public final void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            endElementInternal(QNameUtils.toQName(uri, qName), namespaceContext);
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle endElement: " + ex.getMessage(), ex);
        }
    }

    protected abstract void endElementInternal(QName name, SimpleNamespaceContext namespaceContext)
            throws XMLStreamException;

    public final void characters(char ch[], int start, int length) throws SAXException {
        try {
            charactersInternal(ch, start, length);
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle characters: " + ex.getMessage(), ex);
        }
    }

    protected abstract void charactersInternal(char[] ch, int start, int length) throws XMLStreamException;

    public final void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            ignorableWhitespaceInternal(ch, start, length);
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle ignorableWhitespace:" + ex.getMessage(), ex);
        }
    }

    protected abstract void ignorableWhitespaceInternal(char[] ch, int start, int length) throws XMLStreamException;

    public final void processingInstruction(String target, String data) throws SAXException {
        try {
            processingInstructionInternal(target, data);
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle processingInstruction: " + ex.getMessage(), ex);
        }
    }

    protected abstract void processingInstructionInternal(String target, String data) throws XMLStreamException;

    public final void skippedEntity(String name) throws SAXException {
        try {
            skippedEntityInternal(name);
        }
        catch (XMLStreamException ex) {
            throw new SAXException("Could not handle skippedEntity: " + ex.getMessage(), ex);
        }
    }

    protected abstract void skippedEntityInternal(String name) throws XMLStreamException;
}
