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
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.Assert;

/**
 * @author Arjen Poutsma
 */
public class ListBasedXMLEventReader extends AbstractXMLEventReader {

    private final List<XMLEvent> events;

    private int cursor = 0;

    public ListBasedXMLEventReader(XMLEvent event) {
        Assert.notNull(event, "'event' must not be null");
        this.events = Collections.singletonList(event);
    }

    public ListBasedXMLEventReader(XMLEvent... events) {
        Assert.notNull(events, "'events' must not be null");
        this.events = Arrays.asList(events);
    }

    public ListBasedXMLEventReader(List<XMLEvent> events) {
        Assert.notNull(events, "'events' must not be null");
        this.events = events;
    }

    public boolean hasNext() {
        Assert.notNull(events, "'events' must not be null");
        return cursor != events.size();
    }

    public XMLEvent nextEvent() {
        try {
            return events.get(cursor++);
        }
        catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    public XMLEvent peek() {
        try {
            return events.get(cursor);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
