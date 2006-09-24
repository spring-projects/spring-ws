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

package org.springframework.ws.endpoint;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.mock.MockMessageContext;
import org.springframework.ws.mock.MockWebServiceMessage;

/**
 * Test case for AbstractStaxEventPayloadEndpoint.
 *
 * @see AbstractStaxEventPayloadEndpoint
 */
public class StaxEventPayloadEndpointTest extends XMLTestCase {

    public void testInvoke() throws Exception {
        AbstractStaxEventPayloadEndpoint endpoint = new AbstractStaxEventPayloadEndpoint() {

            protected void invokeInternal(XMLEventReader eventReader,
                                          XMLEventConsumer eventWriter,
                                          XMLEventFactory eventFactory) throws XMLStreamException {
                assertTrue("eventReader has not next element", eventReader.hasNext());
                XMLEvent event = eventReader.nextEvent();
                assertTrue("Not a start document", event.isStartDocument());
                event = eventReader.nextEvent();
                assertTrue("Not a start element", event.isStartElement());
                assertEquals("Invalid start event", "request", event.asStartElement().getName().getLocalPart());
                assertTrue("eventReader has not next element", eventReader.hasNext());
                event = eventReader.nextEvent();
                assertTrue("Not a end element", event.isEndElement());
                assertEquals("Invalid end event", "request", event.asEndElement().getName().getLocalPart());
                eventWriter.add(eventFactory.createStartElement(new QName("response"), null, null));
                eventWriter.add(eventFactory.createEndElement(new QName("response"), null));
            }

        };
        MockMessageContext context = new MockMessageContext("<request/>");
        endpoint.invoke(context);
        assertNotNull("No response created", context.getResponse());
        MockWebServiceMessage response = (MockWebServiceMessage) context.getResponse();
        assertXMLEqual("Invalid response", "<response/>", response.getPayloadAsString());
    }

    public void testInvokeNoResponse() throws Exception {
        AbstractStaxEventPayloadEndpoint endpoint = new AbstractStaxEventPayloadEndpoint() {

            protected void invokeInternal(XMLEventReader eventReader,
                                          XMLEventConsumer eventWriter,
                                          XMLEventFactory eventFactory) throws XMLStreamException {
                assertTrue("eventReader has not next element", eventReader.hasNext());
                XMLEvent event = eventReader.nextEvent();
                assertTrue("Not a start document", event.isStartDocument());
                event = eventReader.nextEvent();
                assertTrue("Not a start element", event.isStartElement());
                assertEquals("Invalid start event", "request", event.asStartElement().getName().getLocalPart());
                assertTrue("eventReader has not next element", eventReader.hasNext());
                event = eventReader.nextEvent();
                assertTrue("Not a end element", event.isEndElement());
                assertEquals("Invalid end event", "request", event.asEndElement().getName().getLocalPart());
            }

        };
        MockMessageContext context = new MockMessageContext("<request/>");
        endpoint.invoke(context);
        assertNull("Response created", context.getResponse());
    }


}
