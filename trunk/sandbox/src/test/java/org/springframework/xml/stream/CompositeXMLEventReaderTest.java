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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arjen Poutsma
 */
public class CompositeXMLEventReaderTest {

    private CompositeXMLEventReader chain;

    private List<XMLEvent> events1 = new ArrayList<XMLEvent>();

    private List<XMLEvent> events3 = new ArrayList<XMLEvent>();

    @Before
    public void createChainUp() throws Exception {
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        events1.add(eventFactory.createStartElement(new QName("event1-1"), null, null));
        events1.add(eventFactory.createEndDocument());
        events1.add(eventFactory.createStartElement(new QName("event1-2"), null, null));
        XMLEventReader reader1 = new ListBasedXMLEventReader(events1);
        XMLEventReader reader2 = new ListBasedXMLEventReader();
        events3.add(eventFactory.createStartElement(new QName("event2-1"), null, null));
        events3.add(eventFactory.createEndElement(new QName("event2-2"), null));
        XMLEventReader reader3 = new ListBasedXMLEventReader(events3);
        XMLEventReader reader4 = new ListBasedXMLEventReader();
        chain = new CompositeXMLEventReader(reader1, reader2, reader3, reader4);
    }

    @Test
    public void testChain() throws Exception {
        assertEquals("peek returns invalid result", events1.get(0), chain.peek());
        assertTrue("hasNext returns false", chain.hasNext());
        assertEquals("nextEvent returns invalid result", events1.get(0), chain.nextEvent());
        assertEquals("peek returns invalid result", events3.get(0), chain.peek());
        assertTrue("hasNext returns false", chain.hasNext());
        assertEquals("nextEvent returns invalid result", events3.get(0), chain.nextEvent());
        assertEquals("peek returns invalid result", events3.get(1), chain.peek());
        assertTrue("hasNext returns false", chain.hasNext());
        assertEquals("nextEvent returns invalid result", events3.get(1), chain.nextEvent());
        assertFalse("hasNext returns true", chain.hasNext());
        assertNull("peek returns element", chain.peek());
        try {
            chain.nextEvent();
            fail("NoSuchElementElementException expected");
        }
        catch (NoSuchElementException e) {
            // expected
        }
    }

}
