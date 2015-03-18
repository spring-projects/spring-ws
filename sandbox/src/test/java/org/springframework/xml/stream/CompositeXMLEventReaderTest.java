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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arjen Poutsma
 */
public class CompositeXMLEventReaderTest {

	private CompositeXMLEventReader chain;

	private XMLInputFactory inputFactory;

	private List<XMLEvent> expectedEvents = new ArrayList<XMLEvent>();

	@Before
	public void createChainUp() throws Exception {
		inputFactory = XMLInputFactory.newFactory();
		List<XMLEvent> events = getEvents("<event1-1><event1-2>text1</event1-2></event1-1>");
		expectedEvents.addAll(events);
		XMLEventReader reader1 = new ListBasedXMLEventReader(events);
		XMLEventReader reader2 = new ListBasedXMLEventReader();
		events = getEvents("<event2-1></event2-1>");
		expectedEvents.addAll(events);
		XMLEventReader reader3 = new ListBasedXMLEventReader(events);
		XMLEventReader reader4 = new ListBasedXMLEventReader();
		chain = new CompositeXMLEventReader(reader1, reader2, reader3, reader4);
	}

	private List<XMLEvent> getEvents(String xml) throws XMLStreamException {
		XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(xml));
		List<XMLEvent> events = new LinkedList<XMLEvent>();
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (!(event.isStartDocument() || event.isEndDocument())) {
				events.add(event);
			}

		}
		return events;
	}

	@Test
	public void testChain() throws Exception {
		for (XMLEvent expectedEvent : expectedEvents) {
			testEvent(expectedEvent);
		}
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

	private void testEvent(XMLEvent expected) throws XMLStreamException {
		assertEquals("1st peek returns invalid result", expected, chain.peek());
		assertEquals("2nd peek returns invalid result", expected, chain.peek());
		assertTrue("hasNext returns false", chain.hasNext());
		assertEquals("nextEvent returns invalid result", expected, chain.nextEvent());
	}

}
