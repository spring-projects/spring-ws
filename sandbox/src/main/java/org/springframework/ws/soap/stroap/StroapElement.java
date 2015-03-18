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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.soap.SoapElement;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.stream.AbstractXMLEventReader;

/**
 * @author Arjen Poutsma
 */
abstract class StroapElement implements SoapElement {

	protected static final String DEFAULT_PREFIX = "SOAP-ENV";

	private final StroapMessageFactory messageFactory;

	private StartElement startElement;

	private EndElement endElement;

	protected StroapElement(QName name, StroapMessageFactory messageFactory) {
		this(createStartElement(name, messageFactory), messageFactory);
	}

	private static StartElement createStartElement(QName name, StroapMessageFactory messageFactory) {
		if (!StringUtils.hasLength(name.getPrefix())) {
			name = new QName(name.getNamespaceURI(), name.getLocalPart(), DEFAULT_PREFIX);
		}
		return messageFactory.getEventFactory().createStartElement(name, null, null);
	}

	protected StroapElement(StartElement startElement, StroapMessageFactory messageFactory) {
		Assert.notNull(startElement, "'startElement' must not be null");
		Assert.notNull(messageFactory, "'messageFactory' must not be null");
		this.messageFactory = messageFactory;
		this.startElement = startElement;
		this.endElement = getEventFactory().createEndElement(startElement.getName(), startElement.getNamespaces());
	}

	public final Source getSource() {
		return StaxUtils.createCustomStaxSource(getEventReader(true));
	}

	protected XMLEventReader getEventReader(boolean documentEvents) {
		return new StroapElementEventReader(documentEvents);
	}

	public void writeTo(XMLEventWriter eventWriter) throws XMLStreamException {
		eventWriter.add(getEventReader(false));
	}

	protected StroapMessageFactory getMessageFactory() {
		return messageFactory;
	}

	protected final XMLEventFactory getEventFactory() {
		return getMessageFactory().getEventFactory();
	}

	protected SoapVersion getSoapVersion() {
		return getMessageFactory().getSoapVersion();
	}

	public final QName getName() {
		return getStartElement().getName();
	}

	protected abstract XMLEventReader getChildEventReader();

	public final Iterator<QName> getAllAttributes() {
		List<QName> result = new LinkedList<QName>();
		for (Iterator iterator = getStartElement().getAttributes(); iterator.hasNext();) {
			Attribute attribute = (Attribute) iterator.next();
			result.add(attribute.getName());
		}
		return result.iterator();
	}

	public final String getAttributeValue(QName name) {
		Attribute attribute = getStartElement().getAttributeByName(name);
		return attribute != null ? attribute.getValue() : null;
	}

	public final void removeAttribute(QName name) {
		List<Attribute> newAttributes = new LinkedList<Attribute>();
		for (Iterator iterator = getStartElement().getAttributes(); iterator.hasNext();) {
			Attribute attribute = (Attribute) iterator.next();
			if (!name.equals(attribute.getName())) {
				newAttributes.add(attribute);
			}
		}
		StartElement oldStartElement = getStartElement();
		this.startElement = getEventFactory().createStartElement(oldStartElement.getName(), newAttributes.iterator(),
				oldStartElement.getNamespaces());
	}

	public final void addAttribute(QName name, String value) {
		List<Attribute> newAttributes = new LinkedList<Attribute>();
		for (Iterator iterator = getStartElement().getAttributes(); iterator.hasNext();) {
			Attribute attribute = (Attribute) iterator.next();
			newAttributes.add(attribute);
		}
		Attribute newAttribute = getEventFactory().createAttribute(name, value);
		newAttributes.add(newAttribute);
		StartElement oldStartElement = getStartElement();
		this.startElement = getEventFactory().createStartElement(oldStartElement.getName(), newAttributes.iterator(),
				oldStartElement.getNamespaces());
	}

	public final void addNamespaceDeclaration(String prefix, String namespaceUri) {
		List<Namespace> newNamespaces = new LinkedList<Namespace>();
		for (Iterator iterator = getStartElement().getNamespaces(); iterator.hasNext();) {
			Namespace namespace = (Namespace) iterator.next();
			newNamespaces.add(namespace);
		}
		Namespace newNamespace;
		if (StringUtils.hasLength(prefix)) {
			newNamespace = getEventFactory().createNamespace(prefix, namespaceUri);
		}
		else {
			newNamespace = getEventFactory().createNamespace(namespaceUri);
		}
		newNamespaces.add(newNamespace);
		StartElement oldStartElement = getStartElement();
		this.startElement = getEventFactory()
				.createStartElement(oldStartElement.getName(), oldStartElement.getAttributes(),
						newNamespaces.iterator());
	}

	protected final StartElement getStartElement() {
		return startElement;
	}

	protected final EndElement getEndElement() {
		return endElement;
	}

	private enum EVENT_READER_STATE {

		START_DOCUMENT,
		START_ELEMENT,
		CHILDREN,
		END_ELEMENT,
		END_DOCUMENT,
		DONE
	}

	private class StroapElementEventReader extends AbstractXMLEventReader {

		private EVENT_READER_STATE state;

		private boolean documentEvents;

		private final XMLEventReader childEventReader;

		private StroapElementEventReader(boolean documentEvents) {
			this.documentEvents = documentEvents;
			state = documentEvents ? EVENT_READER_STATE.START_DOCUMENT : EVENT_READER_STATE.START_ELEMENT;
			this.childEventReader = getChildEventReader();
		}

		public boolean hasNext() {
			if (documentEvents && state == EVENT_READER_STATE.DONE) {
				return false;
			}
			else if (!documentEvents && state == EVENT_READER_STATE.END_DOCUMENT) {
				return false;
			}
			else {
				return true;
			}
		}

		public XMLEvent nextEvent() throws XMLStreamException {
			switch (state) {
				case START_DOCUMENT:
					state = EVENT_READER_STATE.START_ELEMENT;
					return getEventFactory().createStartDocument();
				case START_ELEMENT:
					state = EVENT_READER_STATE.CHILDREN;
					return getStartElement();
				case CHILDREN:
					if (!childEventReader.hasNext()) {
						state = EVENT_READER_STATE.END_ELEMENT;
						return nextEvent();
					}
					return childEventReader.nextEvent();
				case END_ELEMENT:
					state = EVENT_READER_STATE.END_DOCUMENT;
					return getEndElement();
				case END_DOCUMENT:
					state = EVENT_READER_STATE.DONE;
					return getEventFactory().createEndDocument();
				case DONE:
					throw new NoSuchElementException();
				default:
					throw new IllegalStateException();
			}
		}

		public XMLEvent peek() throws XMLStreamException {
			switch (state) {
				case START_DOCUMENT:
					return getEventFactory().createStartDocument();
				case START_ELEMENT:
					return getStartElement();
				case CHILDREN:
					XMLEvent event = childEventReader.peek();
					if (event == null) {
						state = EVENT_READER_STATE.END_ELEMENT;
						event = getEndElement();
					}
					return event;
				case END_ELEMENT:
					return getEndElement();
				case END_DOCUMENT:
					return getEventFactory().createEndDocument();
				case DONE:
					return null;
				default:
					throw new IllegalStateException();
			}

		}
	}
}
