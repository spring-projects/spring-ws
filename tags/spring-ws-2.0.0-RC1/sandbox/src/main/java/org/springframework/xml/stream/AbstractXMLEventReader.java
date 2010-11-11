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

import java.util.NoSuchElementException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.ClassUtils;

/**
 * Abstract base class for <code>XMLEventReader</code>s.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractXMLEventReader implements XMLEventReader {

    private boolean closed;

    public Object next() {
        try {
            return nextEvent();
        }
        catch (XMLStreamException ex) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Throws an <code>UnsupportedOperationException</code> when called.
     *
     * @throws UnsupportedOperationException when called
     */
    public void remove() {
        throw new UnsupportedOperationException("remove not supported on " + ClassUtils.getShortName(getClass()));
    }

    public String getElementText() throws XMLStreamException {
        checkIfClosed();
        if (!peek().isStartElement()) {
            throw new XMLStreamException("Not at START_ELEMENT");
        }

        StringBuilder builder = new StringBuilder();
        while (true) {
            XMLEvent event = nextEvent();
            if (event.isEndElement()) {
                break;
            }
            else if (!event.isCharacters()) {
                throw new XMLStreamException("Unexpected event [" + event + "] in getElementText()");
            }
            Characters characters = event.asCharacters();
            if (!characters.isIgnorableWhiteSpace()) {
                builder.append(event.asCharacters().getData());
            }
        }
        return builder.toString();
    }

    public XMLEvent nextTag() throws XMLStreamException {
        checkIfClosed();
        while (true) {
            XMLEvent event = nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                case XMLStreamConstants.END_ELEMENT:
                    return event;
                case XMLStreamConstants.END_DOCUMENT:
                    return null;
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.COMMENT:
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    continue;
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.CHARACTERS:
                    if (!event.asCharacters().isWhiteSpace()) {
                        throw new XMLStreamException("Non-ignorable whitespace CDATA or CHARACTERS event in nextTag()");
                    }
                    break;
                default:
                    throw new XMLStreamException(
                            "Received event [" + event + "], instead of START_ELEMENT or END_ELEMENT.");
            }
        }
    }

    /**
     * Throws an <code>IllegalArgumentException</code> when called.
     *
     * @throws IllegalArgumentException when called.
     */
    public Object getProperty(String name) throws IllegalArgumentException {
        throw new IllegalArgumentException("Property not supported: [" + name + "]");
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
            throw new XMLStreamException("XMLEventReader has been closed");
        }
    }

    public void close() {
        closed = true;
    }
}
