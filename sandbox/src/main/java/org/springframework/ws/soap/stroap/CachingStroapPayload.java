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

package org.springframework.ws.soap.stroap;

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.springframework.util.Assert;
import org.springframework.xml.stream.ListBasedXMLEventReader;

/**
 * @author Arjen Poutsma
 */
class CachingStroapPayload extends StroapPayload {

	private final List<XMLEvent> events = new LinkedList<XMLEvent>();

	CachingStroapPayload() {
	}

	CachingStroapPayload(XMLEventReader eventReader) throws XMLStreamException {
		Assert.notNull(eventReader, "'eventReader' must not be null");
		XMLEventWriter eventWriter = getEventWriter();
		eventWriter.add(eventReader);
	}

	@Override
	public QName getName() {
		if (!events.isEmpty()) {
			XMLEvent event = events.get(0);
			if (event.isStartElement()) {
				return event.asStartElement().getName();
			}
		}
		return null;
	}

	@Override
	public XMLEventReader getEventReader() {
		return new ListBasedXMLEventReader(events);
	}

	public XMLEventWriter getEventWriter() {
		events.clear();
		return new CachingXMLEventWriter(events);
	}

}