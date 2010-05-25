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

package org.springframework.xml.stream;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * SAX <code>ContentHandler</code> that writes to a <code>XMLStreamWriter</code>.
 *
 * @author Arjen Poutsma
 * @see XMLStreamWriter
 * @since 1.0.0
 */
@SuppressWarnings("Since15")
public class StaxStreamContentHandler extends AbstractStaxContentHandler {

    private final XMLStreamWriter streamWriter;

    /**
     * Constructs a new instance of the <code>StaxStreamContentHandler</code> that writes to the given
     * <code>XMLStreamWriter</code>.
     *
     * @param streamWriter the stream writer to write to
     */
    public StaxStreamContentHandler(XMLStreamWriter streamWriter) {
        this.streamWriter = streamWriter;
    }

    public void setDocumentLocator(Locator locator) {
    }

    @Override
    protected void charactersInternal(char[] ch, int start, int length) throws XMLStreamException {
        streamWriter.writeCharacters(ch, start, length);
    }

    @Override
    protected void endDocumentInternal() throws XMLStreamException {
        streamWriter.writeEndDocument();
    }

    @Override
    protected void endElementInternal(QName name, SimpleNamespaceContext namespaceContext) throws XMLStreamException {
        streamWriter.writeEndElement();
    }

    @Override
    protected void ignorableWhitespaceInternal(char[] ch, int start, int length) throws XMLStreamException {
        streamWriter.writeCharacters(ch, start, length);
    }

    @Override
    protected void processingInstructionInternal(String target, String data) throws XMLStreamException {
        streamWriter.writeProcessingInstruction(target, data);
    }

    @Override
    protected void skippedEntityInternal(String name) {
    }

    @Override
    protected void startDocumentInternal() throws XMLStreamException {
        streamWriter.writeStartDocument();
    }

    @Override
    protected void startElementInternal(QName name, Attributes attributes, SimpleNamespaceContext namespaceContext)
            throws XMLStreamException {
        streamWriter.writeStartElement(QNameUtils.getPrefix(name), name.getLocalPart(), name.getNamespaceURI());
        String defaultNamespaceUri = namespaceContext.getNamespaceURI("");
        if (StringUtils.hasLength(defaultNamespaceUri)) {
            streamWriter.writeNamespace("", defaultNamespaceUri);
            streamWriter.setDefaultNamespace(defaultNamespaceUri);
        }
        for (Iterator<String> iterator = namespaceContext.getBoundPrefixes(); iterator.hasNext();) {
            String prefix = iterator.next();
            streamWriter.writeNamespace(prefix, namespaceContext.getNamespaceURI(prefix));
            streamWriter.setPrefix(prefix, namespaceContext.getNamespaceURI(prefix));
        }
        for (int i = 0; i < attributes.getLength(); i++) {
            QName attrName = QNameUtils.toQName(attributes.getURI(i), attributes.getQName(i));
            String attrPrefix = QNameUtils.getPrefix(attrName);
            if (!("xmlns".equals(attrName.getLocalPart()) || "xmlns".equals(attrPrefix))) {
                streamWriter.writeAttribute(attrPrefix, attrName.getNamespaceURI(), attrName.getLocalPart(),
                        attributes.getValue(i));
            }
        }
    }
}
