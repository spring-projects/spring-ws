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

package org.springframework.ws.server.endpoint;

import java.util.Collections;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import static org.junit.Assert.*;

/**
 * Test case for AbstractStaxEventPayloadEndpoint.
 *
 * @see AbstractStaxEventPayloadEndpoint
 */
@SuppressWarnings("Since15")
public class StaxEventPayloadEndpointTest extends AbstractMessageEndpointTestCase {

	@Override
	protected MessageEndpoint createNoResponseEndpoint() {
		return new AbstractStaxEventPayloadEndpoint() {
			@Override
			protected void invokeInternal(XMLEventReader eventReader,
										  XMLEventConsumer eventWriter,
										  XMLEventFactory eventFactory) throws Exception {
				assertNotNull("No EventReader passed", eventReader);
			}
		};
	}

	@Override
	protected MessageEndpoint createNoRequestPayloadEndpoint() {
		return new AbstractStaxEventPayloadEndpoint() {

			@Override
			protected void invokeInternal(XMLEventReader eventReader,
										  XMLEventConsumer eventWriter,
										  XMLEventFactory eventFactory) throws Exception {
				assertNull("EventReader passed", eventReader);
			}
		};
	}

	@Override
	protected MessageEndpoint createResponseEndpoint() {
		return new AbstractStaxEventPayloadEndpoint() {

			@Override
			protected void invokeInternal(XMLEventReader eventReader,
										  XMLEventConsumer eventWriter,
										  XMLEventFactory eventFactory) throws XMLStreamException {
				assertNotNull("eventReader not given", eventReader);
				assertNotNull("eventWriter not given", eventWriter);
				assertNotNull("eventFactory not given", eventFactory);
				assertTrue("eventReader has not next element", eventReader.hasNext());
				XMLEvent event = eventReader.nextEvent();
				assertTrue("Not a start document", event.isStartDocument());
				event = eventReader.nextEvent();
				assertTrue("Not a start element", event.isStartElement());
				assertEquals("Invalid start event local name", REQUEST_ELEMENT,
						event.asStartElement().getName().getLocalPart());
				assertEquals("Invalid start event namespace", NAMESPACE_URI,
						event.asStartElement().getName().getNamespaceURI());
				assertTrue("eventReader has not next element", eventReader.hasNext());
				event = eventReader.nextEvent();
				assertTrue("Not a end element", event.isEndElement());
				assertEquals("Invalid end event local name", REQUEST_ELEMENT,
						event.asEndElement().getName().getLocalPart());
				assertEquals("Invalid end event namespace", NAMESPACE_URI,
						event.asEndElement().getName().getNamespaceURI());
				Namespace namespace = eventFactory.createNamespace(NAMESPACE_URI);
				QName name = new QName(NAMESPACE_URI, RESPONSE_ELEMENT);
				eventWriter
						.add(eventFactory.createStartElement(name, null, Collections.singleton(namespace).iterator()));
				eventWriter.add(eventFactory.createEndElement(name, Collections.singleton(namespace).iterator()));
				eventWriter.add(eventFactory.createEndDocument());
			}
		};
	}


}
