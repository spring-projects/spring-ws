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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.Assert;

/**
 * @author Arjen Poutsma
 */
public class CompositeXMLEventReader extends AbstractXMLEventReader {

    private List<XMLEventReader> eventReaders;

    private int cursor = 0;

    public CompositeXMLEventReader(XMLEventReader eventReader) {
        Assert.notNull(eventReader, "'eventReader' must not be null");
        this.eventReaders = Collections.singletonList(eventReader);
    }

    public CompositeXMLEventReader(XMLEventReader... eventReaders) {
        Assert.notNull(eventReaders, "'eventReaders' must not be null");
        this.eventReaders = Arrays.asList(eventReaders);
    }

    public CompositeXMLEventReader(List<XMLEventReader> eventReaders) {
        Assert.notNull(eventReaders, "'eventReaders' must not be null");
        this.eventReaders = eventReaders;
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        else {
            return eventReaders.get(cursor).nextEvent();
        }
    }

    public boolean hasNext() {
        try {
            while (cursor < eventReaders.size()) {
                if (cursor != eventReaders.size() - 1) {
                    XMLEvent event = eventReaders.get(cursor).peek();
                    if (event == null || event.isEndDocument()) {
                        cursor++;
                        continue;
                    }
                }
                return eventReaders.get(cursor).hasNext();
            }
        }
        catch (XMLStreamException ex) {
            // ignored
        }
        return false;
    }

    public XMLEvent peek() throws XMLStreamException {
        if (hasNext()) {
            return eventReaders.get(cursor).peek();
        }
        return null;
    }

}
