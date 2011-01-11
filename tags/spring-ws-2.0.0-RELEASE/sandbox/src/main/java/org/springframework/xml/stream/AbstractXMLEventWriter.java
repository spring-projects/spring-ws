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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;

/**
 * @author Arjen Poutsma
 */
public abstract class AbstractXMLEventWriter implements XMLEventWriter {

    private boolean closed;

    private SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();

    public void flush() throws XMLStreamException {
    }

    public void add(XMLEventReader eventReader) throws XMLStreamException {
        checkIfClosed();
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            add(event);
        }
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return namespaceContext.getPrefix(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        namespaceContext.bindNamespaceUri(prefix, uri);
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        namespaceContext.bindDefaultNamespaceUri(uri);
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        Assert.notNull(namespaceContext, "'namespaceContext' must not be null");
        this.namespaceContext = (SimpleNamespaceContext) namespaceContext;
    }

    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Returns <code>true</code> if closed; <code>false</code> otherwise.
     *
     * @see #close()
     */
    protected boolean isClosed() {
        return closed;
    }

    /**
     * Checks if the reader is closed, and throws a <code>XMLStreamException</code> if so.
     *
     * @throws XMLStreamException if the reader is closed
     * @see #close()
     * @see #isClosed()
     */
    protected void checkIfClosed() throws XMLStreamException {
        if (closed) {
            throw new XMLStreamException(ClassUtils.getShortName(getClass()) + " has been closed");
        }
    }

    public void close() {
        closed = true;
    }

}
