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

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

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
			protected void invokeInternal(XMLEventReader eventReader, XMLEventConsumer eventWriter,
					XMLEventFactory eventFactory) {
				assertThat(eventReader).isNotNull();
			}
		};
	}

	@Override
	protected MessageEndpoint createNoRequestPayloadEndpoint() {

		return new AbstractStaxEventPayloadEndpoint() {

			@Override
			protected void invokeInternal(XMLEventReader eventReader, XMLEventConsumer eventWriter,
					XMLEventFactory eventFactory) {
				assertThat(eventReader).isNull();
			}
		};
	}

	@Override
	protected MessageEndpoint createResponseEndpoint() {

		return new AbstractStaxEventPayloadEndpoint() {

			@Override
			protected void invokeInternal(XMLEventReader eventReader, XMLEventConsumer eventWriter,
					XMLEventFactory eventFactory) throws XMLStreamException {

				assertThat(eventReader).isNotNull();
				assertThat(eventWriter).isNotNull();
				assertThat(eventFactory).isNotNull();
				assertThat(eventReader.hasNext()).isTrue();

				XMLEvent event = eventReader.nextEvent();

				assertThat(event.isStartDocument()).isTrue();

				event = eventReader.nextEvent();

				assertThat(event.isStartElement()).isTrue();
				assertThat(event.asStartElement().getName().getLocalPart()).isEqualTo(REQUEST_ELEMENT);
				assertThat(event.asStartElement().getName().getNamespaceURI()).isEqualTo(NAMESPACE_URI);
				assertThat(eventReader.hasNext()).isTrue();

				event = eventReader.nextEvent();

				assertThat(event.isEndElement()).isTrue();
				assertThat(event.asEndElement().getName().getLocalPart()).isEqualTo(REQUEST_ELEMENT);
				assertThat(event.asEndElement().getName().getNamespaceURI()).isEqualTo(NAMESPACE_URI);

				Namespace namespace = eventFactory.createNamespace(NAMESPACE_URI);
				QName name = new QName(NAMESPACE_URI, RESPONSE_ELEMENT);

				eventWriter.add(eventFactory.createStartElement(name, null, Collections.singleton(namespace).iterator()));
				eventWriter.add(eventFactory.createEndElement(name, Collections.singleton(namespace).iterator()));
				eventWriter.add(eventFactory.createEndDocument());
			}
		};
	}

}
